package edu.sc.seis.sod.source.event;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.w3c.dom.Element;
import com.csvreader.CsvReader;
import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.FlinnEngdahlType;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.Unit;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.FlinnEngdahlRegionImpl;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnsupportedFormat;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.subsetter.AreaSubsetter;

/**
 * @author oliverpa
 * 
 * Created on Jul 1, 2005
 */
public class CSVEventSource extends SimpleEventSource {

    public CSVEventSource(Element config) throws ConfigurationException {
        csvFilename = DOMHelper.extractText(config, "filename");
        try {
            events = getEventsFromCSVFile(csvFilename);
        } catch(FileNotFoundException e) {
            throw new UserConfigurationException(e.getMessage()
                    + " as a event CSV file.");
        } catch(IOException e) {
            throw new ConfigurationException("Unable to read " + csvFilename, e);
        }
    }

    public CacheEvent[] getEvents() {
        return events;
    }

    public static CacheEvent[] getEventsFromCSVFile(String filename)
            throws FileNotFoundException, IOException, ConfigurationException {
        return getEventsFromReader(AreaSubsetter.makeRelativeOrRecipeDirReader(filename));
    }

    public static CacheEvent[] getEventsFromReader(Reader reader)
            throws IOException, FileNotFoundException, ConfigurationException {
        List events = new ArrayList();
        CsvReader csvReader = new CsvReader(reader);
        csvReader.readHeaders();
        List headers = Arrays.asList(csvReader.getHeaders());
        for(int i = 0; i < headers.size(); i++) {
            String cur = (String)headers.get(i);
            if(!isValidField(cur)) {
                throw new UserConfigurationException(cur
                        + " is not a known CSV field.  "
                        + concatenateValidFields() + " are valid options.");
            }
        }
        while(csvReader.readRecord()) {
            // time to start populating field values
            // first up: the only required field...
            Time time = new Time(csvReader.get(TIME), 0);
            try {
                new ISOTime(time.date_time);
            } catch(UnsupportedFormat uf) {
                throw new UserConfigurationException("The time '"
                        + time.date_time + "' in record "
                        + csvReader.getCurrentRecord() + " is invalid.");
            }
            float latitude = 0f;
            if(headers.contains(LATITUDE)) {
                latitude = Float.parseFloat(csvReader.get(LATITUDE));
            }
            float longitude = 0f;
            if(headers.contains(LONGITUDE)) {
                longitude = Float.parseFloat(csvReader.get(LONGITUDE));
            }
            double depth = 0f;
            if(headers.contains(DEPTH)) {
                depth = Double.parseDouble(csvReader.get(DEPTH));
            }
            Unit depthUnit = UnitImpl.KILOMETER;
            if(headers.contains(DEPTH_UNITS)) {
                String unitName = csvReader.get(DEPTH_UNITS);
                try {
                    depthUnit = UnitImpl.getUnitFromString(unitName);
                } catch(NoSuchFieldException e) {
                    throw new UserConfigurationException(unitName
                            + " in record "
                            + csvReader.getCurrentRecord()
                            + " is not a valid unit name.  Try KILOMETER or METER");
                }
            }
            Location location = new Location(latitude,
                                             longitude,
                                             new QuantityImpl(0.0,
                                                              UnitImpl.METER),
                                             new QuantityImpl(depth, depthUnit),
                                             LocationType.GEOGRAPHIC);
            String defaultString = "csvEvent";
            String catalog = defaultString;
            if(headers.contains(CATALOG)) {
                catalog = csvReader.get(CATALOG);
            }
            String contributor = defaultString;
            if(headers.contains(CONTRIBUTOR)) {
                contributor = csvReader.get(CONTRIBUTOR);
            }
            Magnitude[] magnitudes = new Magnitude[0];
            String[] magValues, magTypes, magContribs;
            if(headers.contains(MAGNITUDE)) {
                magValues = parseValuesFromSeparatedValues(csvReader.get(MAGNITUDE),
                                                           ':',
                                                           "0");
            } else {
                magValues = new String[] {"0"};
            }
            if(magValues.length > 1 || (headers.contains(MAGNITUDE_TYPE))) {
                magTypes = parseValuesFromSeparatedValues(csvReader.get(MAGNITUDE_TYPE),
                                                          ':',
                                                          "M");
                if(magTypes.length != magValues.length) {
                    throw new UserConfigurationException("count of magnitude types does not match count of magnitude values in record "
                            + csvReader.getCurrentRecord());
                }
            } else {
                magTypes = new String[] {"M"};
            }
            if(magValues.length > 1 || headers.contains(MAGNITUDE_CONTRIBUTOR)) {
                magContribs = parseValuesFromSeparatedValues(csvReader.get(MAGNITUDE_CONTRIBUTOR),
                                                             ':',
                                                             defaultString);
                if(magContribs.length != magValues.length) {
                    throw new UserConfigurationException("count of magnitude types does not match count of magnitude values in record "
                            + csvReader.getCurrentRecord());
                }
            } else {
                magContribs = new String[] {defaultString};
            }
            magnitudes = new Magnitude[magValues.length];
            for(int i = 0; i < magValues.length; i++) {
                magnitudes[i] = new Magnitude(magTypes[i],
                                              Float.parseFloat(magValues[i]),
                                              magContribs[i]);
            }
            Origin origin = new OriginImpl("",
                                           catalog,
                                           contributor,
                                           time,
                                           location,
                                           magnitudes,
                                           new ParameterRef[0]);
            String name = defaultString;
            if(headers.contains(NAME)) {
                name = csvReader.get(NAME);
            }
            FlinnEngdahlType feType = FlinnEngdahlType.SEISMIC_REGION;
            if(headers.contains(FE_REGION_TYPE)) {
                int type = Integer.parseInt(csvReader.get(FE_REGION_TYPE));
                if(type == FlinnEngdahlType._SEISMIC_REGION) {
                    feType = FlinnEngdahlType.SEISMIC_REGION;
                } else if(type == FlinnEngdahlType._GEOGRAPHIC_REGION) {
                    feType = FlinnEngdahlType.GEOGRAPHIC_REGION;
                }
            }
            FlinnEngdahlRegion feRegion = new FlinnEngdahlRegionImpl(feType, 0);
            if(headers.contains(FE_REGION)) {
                feRegion = new FlinnEngdahlRegionImpl(feType,
                                                      Integer.parseInt(csvReader.get(FE_REGION)));
            }
            events.add(new CacheEvent(new EventAttrImpl(name, feRegion), origin));
        }
        return (CacheEvent[])events.toArray(new CacheEvent[0]);
    }

    private static String[] parseValuesFromSeparatedValues(String uberValue,
                                                           char delim,
                                                           String defaultVal) {
        List values = new ArrayList();
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < uberValue.length(); i++) {
            char curChar = uberValue.charAt(i);
            if(curChar == delim) {
                buf = readCurrentValue(defaultVal, values, buf);
            } else {
                buf.append(curChar);
            }
        }
        readCurrentValue(defaultVal, values, buf);
        return (String[])values.toArray(new String[0]);
    }

    private static StringBuffer readCurrentValue(String defaultVal,
                                                 List values,
                                                 StringBuffer buf) {
        if(buf.length() == 0) {
            values.add(defaultVal);
            return buf;
        } else {
            values.add(buf.toString());
            return new StringBuffer();
        }
    }

    public static String concatenateValidFields() {
        String allFields = "";
        for(int i = 0; i < FIELDS.length - 1; i++) {
            allFields += FIELDS[i] + ", ";
        }
        return allFields + FIELDS[FIELDS.length - 1];
    }

    private static boolean isValidField(String field) {
        for(int i = 0; i < FIELDS.length; i++) {
            if(field.equals(FIELDS[i])) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return "CSVEventSource using " + csvFilename;
    }

    private CacheEvent[] events;

    // required
    public static final String TIME = "time";

    // optional
    public static final String LONGITUDE = "longitude";

    public static final String LATITUDE = "latitude";

    public static final String DEPTH = "depth";

    public static final String MAGNITUDE = "magnitude";

    public static final String CATALOG = "catalog";

    public static final String CONTRIBUTOR = "contributor";

    public static final String NAME = "name";

    public static final String FE_SEIS_REGION = "flinnEngdahlSeismicRegion";

    public static final String FE_GEO_REGION = "flinnEngdahlGeographicRegion";

    public static final String FE_REGION = "flinnEngdahlRegion";

    public static final String FE_REGION_TYPE = "flinnEngdahlRegionType";

    // defaultable
    public static final String DEPTH_UNITS = "depthUnits";

    public static final String MAGNITUDE_TYPE = "magnitudeType";

    public static final String MAGNITUDE_CONTRIBUTOR = "magnitudeContributor";

    private static final String[] FIELDS = new String[] {TIME,
                                                         LONGITUDE,
                                                         LATITUDE,
                                                         DEPTH,
                                                         DEPTH_UNITS,
                                                         MAGNITUDE,
                                                         MAGNITUDE_TYPE,
                                                         MAGNITUDE_CONTRIBUTOR,
                                                         CATALOG,
                                                         CONTRIBUTOR,
                                                         NAME,
                                                         FE_SEIS_REGION,
                                                         FE_GEO_REGION,
                                                         FE_REGION,
                                                         FE_REGION_TYPE};

    private String csvFilename;
}
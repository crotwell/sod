package edu.sc.seis.sod.source.event;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.w3c.dom.Element;
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
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.UserConfigurationException;

/**
 * @author oliverpa
 * 
 * Created on Jul 1, 2005
 */
public class CSVEventSource extends SimpleEventSource {

    public CSVEventSource(Element config) throws ConfigurationException {
        String filename = DOMHelper.extractText(config, "filename");
        try {
            events = getEventsFromCSVFile(filename);
        } catch(FileNotFoundException e) {
            throw new UserConfigurationException("CSV event file '" + filename
                    + "' not found.");
        } catch(IOException e) {
            throw new ConfigurationException("Unable to read " + filename, e);
        }
    }

    public CacheEvent[] getEvents() {
        return events;
    }

    public static CacheEvent[] getEventsFromCSVFile(String filename)
            throws IOException, FileNotFoundException, ConfigurationException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        // let's get the fields
        List fields = new ArrayList();
        String line = reader.readLine();
        if(line != null) {
            StringTokenizer tok = new StringTokenizer(line, ", ");
            while(tok.hasMoreTokens()) {
                String cur = tok.nextToken().trim();
                if(isValidField(cur)) {
                    fields.add(cur);
                } else {
                    String allFields = getHeader();
                    throw new UserConfigurationException(cur
                            + " is not a known CSV field.  " + allFields
                            + " are valid options.");
                }
            }
        } else {
            throw new UserConfigurationException("No header row in csv file "
                    + filename + ".");
        }
        if(!fields.contains("time")) {
            throw new UserConfigurationException("Required header 'time' not found in csv event file "
                    + filename + ".");
        }
        // let's get the events!
        List events = new ArrayList();
        int lineNum = 1;
        while((line = reader.readLine()) != null) {
            lineNum++;
            List values = new ArrayList();
            if(line.startsWith("#")) {// Comment line
                continue;
            }
            StringTokenizer tok = new StringTokenizer(line, ",");
            while(tok.hasMoreTokens()) {
                values.add(tok.nextToken().trim());
            }
            if(values.size() == 0) { // Empty line
                continue;
            }
            if(values.size() != fields.size()) {
                throw new UserConfigurationException("There are "
                        + values.size() + " values on line " + lineNum
                        + " but there are " + fields.size()
                        + " in the header in csv files" + filename + ".");
            }
            // time to start populating field values
            // first up: the only required field...
            Time time = new Time((String)values.get(fields.indexOf(TIME)), 0);
            float latitude = 0f;
            if(fields.contains(LATITUDE)) {
                latitude = Float.parseFloat((String)values.get(fields.indexOf(LATITUDE)));
            }
            float longitude = 0f;
            if(fields.contains(LONGITUDE)) {
                longitude = Float.parseFloat((String)values.get(fields.indexOf(LONGITUDE)));
            }
            double depth = 0f;
            if(fields.contains(DEPTH)) {
                depth = Double.parseDouble((String)values.get(fields.indexOf(DEPTH)));
            }
            Unit depthUnit = UnitImpl.METER;
            if(fields.contains(DEPTH_UNITS)) {
                String unitName = (String)values.get(fields.indexOf(DEPTH_UNITS));
                try {
                    depthUnit = UnitImpl.getUnitFromString(unitName);
                } catch(NoSuchFieldException e) {
                    throw new UserConfigurationException(unitName
                            + " on line "
                            + lineNum
                            + " in "
                            + filename
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
            if(fields.contains(CATALOG)) {
                catalog = (String)values.get(fields.indexOf(CATALOG));
            }
            String contributor = defaultString;
            if(fields.contains(CONTRIBUTOR)) {
                contributor = (String)values.get(fields.indexOf(CONTRIBUTOR));
            }
            String magType = "M";
            if(fields.contains(MAGNITUDE_TYPE)) {
                magType = (String)values.get(fields.indexOf(MAGNITUDE_TYPE));
            }
            float magVal = 0f;
            if(fields.contains(MAGNITUDE)) {
                magVal = Float.parseFloat((String)values.get(fields.indexOf(MAGNITUDE)));
            }
            Magnitude magnitude = new Magnitude(magType, magVal, contributor);
            Origin origin = new OriginImpl("",
                                           catalog,
                                           contributor,
                                           time,
                                           location,
                                           new Magnitude[] {magnitude},
                                           new ParameterRef[0]);
            String name = defaultString;
            if(fields.contains(NAME)) {
                name = (String)values.get(fields.indexOf(NAME));
            }
            FlinnEngdahlType feType = FlinnEngdahlType.SEISMIC_REGION;
            if(fields.contains(FE_REGION_TYPE)) {
                int type = Integer.parseInt((String)values.get(fields.indexOf(FE_REGION_TYPE)));
                if(type == FlinnEngdahlType._SEISMIC_REGION) {
                    feType = FlinnEngdahlType.SEISMIC_REGION;
                } else if(type == FlinnEngdahlType._GEOGRAPHIC_REGION) {
                    feType = FlinnEngdahlType.GEOGRAPHIC_REGION;
                }
            }
            FlinnEngdahlRegion feRegion = new FlinnEngdahlRegionImpl(feType, 0);
            if(fields.contains(FE_REGION)) {
                feRegion = new FlinnEngdahlRegionImpl(feType,
                                                      Integer.parseInt((String)values.get(fields.indexOf(FE_REGION))));
            }
            events.add(new CacheEvent(new EventAttrImpl(name, feRegion), origin));
        }
        return (CacheEvent[])events.toArray(new CacheEvent[0]);
    }

    public static String getHeader() {
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

    private static final String[] FIELDS = new String[] {TIME,
                                                         LONGITUDE,
                                                         LATITUDE,
                                                         DEPTH,
                                                         MAGNITUDE,
                                                         CATALOG,
                                                         CONTRIBUTOR,
                                                         NAME,
                                                         FE_SEIS_REGION,
                                                         FE_GEO_REGION,
                                                         FE_REGION,
                                                         FE_REGION_TYPE,
                                                         DEPTH_UNITS};
}
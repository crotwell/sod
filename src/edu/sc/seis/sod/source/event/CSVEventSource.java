package edu.sc.seis.sod.source.event;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
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
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;

/**
 * @author oliverpa
 * 
 * Created on Jul 1, 2005
 */
public class CSVEventSource extends SimpleEventSource {

    public CSVEventSource(Element config) throws ConfigurationException {
        Element filenameEl = SodUtil.getElement(config, "filename");
        try {
            events = getEventsFromCSVFile(SodUtil.getNestedText(filenameEl));
        } catch(FileNotFoundException e) {
            throw new ConfigurationException("File '"+SodUtil.getNestedText(filenameEl)+"' does not seem to exist?",
                                             e);
        } catch(Exception e) {
            throw new ConfigurationException("problem loading events from file: "+SodUtil.getNestedText(filenameEl),
                                             e);
        }
    }

    public CacheEvent[] getEvents() {
        return events;
    }

    public static CacheEvent[] getEventsFromCSVFile(String filename)
            throws IOException, FileNotFoundException, ConfigurationException,
            NoSuchFieldException {
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
                    throw new ConfigurationException("invalid field in csv file");
                }
            }
        } else {
            throw new ConfigurationException("no header row in csv file");
        }
        // let's get the events!
        List events = new ArrayList();
        while((line = reader.readLine()) != null) {
            List values = new ArrayList();
            StringTokenizer tok = new StringTokenizer(line, ",");
            while(tok.hasMoreTokens()) {
                values.add(tok.nextToken().trim());
            }
            if(values.size() != fields.size()) {
                throw new ConfigurationException("field-value row size descrepency");
            }
            // time to start populating field values
            // first up: the only required field...
            Time time = null;
            if(fields.contains(TIME)) {
                time = new Time((String)values.get(fields.indexOf(TIME)), 0);
            } else {
                throw new ConfigurationException("csv entry missing required field 'time'");
            }
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
                depthUnit = UnitImpl.getUnitFromString((String)values.get(fields.indexOf(DEPTH_UNITS)));
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

    private static boolean isValidField(String field) {
        return field.equals(TIME) || field.equals(LONGITUDE)
                || field.equals(LATITUDE) || field.equals(DEPTH)
                || field.equals(MAGNITUDE) || field.equals(CATALOG)
                || field.equals(CONTRIBUTOR) || field.equals(NAME)
                || field.equals(FE_REGION) || field.equals(FE_REGION_TYPE)
                || field.equals(DEPTH_UNITS) || field.equals(MAGNITUDE_TYPE);
    }

    private CacheEvent[] events;

    private static Logger logger = Logger.getLogger(CSVEventSource.class);

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
}
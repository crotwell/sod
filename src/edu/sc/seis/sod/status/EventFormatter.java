package edu.sc.seis.sod.status;


import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.event.MagnitudeUtil;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.event.StatefulEvent;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import edu.sc.seis.sod.status.eventArm.EventTemplate;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class EventFormatter extends Template implements EventTemplate{

    public EventFormatter(boolean filize) throws ConfigurationException   {
        this(null, filize);
    }

    public EventFormatter(Element config) throws ConfigurationException  {
        this(config, false);
    }

    public EventFormatter(Element config, boolean filize) throws ConfigurationException {
        if(config == null || config.hasChildNodes() == false) useDefaultConfig();
        else parse(config);
        filizeResults = filize;
    }

    public static EventFormatter getDefaultFormatter() {
        try {
            if(defaultFormatter == null) defaultFormatter = new EventFormatter(false);
        } catch (ConfigurationException e) {
            // this should never happen as default is constructed with a null element
            GlobalExceptionHandler.handle("This should never have happened, there is a bug in the default EventFormater.", e);
            throw new RuntimeException("Got configuration exception for default event formater", e);
        }
        return defaultFormatter;
    }

    public static String getDefaultResult(EventAccessOperations event)  {
        return defaultFormatter.getResult(event);
    }

    private static EventFormatter defaultFormatter;

    protected Object textTemplate(final String text) {
        return new EventTemplate(){
            public String getResult(EventAccessOperations ev) { return text; }
        };
    }

    protected Object getTemplate(String tag, Element el) {
        if(tag.equals("feRegionName")) {
            return new RegionName();
        } else if(tag.equals("feRegionNumber")) {
            return new EventTemplate(){
                public String getResult(EventAccessOperations ev){
                    return Integer.toString(ev.get_attributes().region.number);
                }
            };
        } else if(tag.equals("depth")) {
            return new EventTemplate(){
                public String getResult(EventAccessOperations ev){
                    return UnitDisplayUtil.formatQuantityImpl(getOrigin(ev).my_location.depth);
                }
            };
        } else if(tag.equals("latitude")) {
            return new EventTemplate(){
                public String getResult(EventAccessOperations ev){
                    return format(getOrigin(ev).my_location.latitude);
                }
            };
        } else if(tag.equals("longitude")) {
            return new EventTemplate(){public String getResult(EventAccessOperations ev){
                    return format(getOrigin(ev).my_location.longitude);
                }
            };
        } else if(tag.equals("magnitude")) {
            return new EventTemplate(){
                public String getResult(EventAccessOperations ev) {
                    return getMag(ev);
                }
            };
        } else if(tag.equals("originTime")) {
            return new Time(SodUtil.getText((el)));
        } else if(tag.equals("eventStatus")) {
            return new EventStatusFormatter();
        }else if(tag.equals("waveformChannels")){
            try {
                return new EventChannelQuery(SodUtil.getNestedText(el));
            } catch (NoSuchFieldException e) {
                GlobalExceptionHandler.handle("Unknown standing name passed into event formatter",
                                              e);
            }
        }
        return super.getCommonTemplate(tag, el);
    }

    private class EventChannelQuery implements EventTemplate{
        public EventChannelQuery(String standing) throws NoSuchFieldException{
            Standing s = Standing.getForName(standing);
            if(s.equals(Standing.SUCCESS)){
                statii.add(Status.get(Stage.PROCESSOR, s));
            }else if(s.equals(Standing.REJECT)){
                statii.add(Status.get(Stage.EVENT_CHANNEL_SUBSETTER, s));
                statii.add(Status.get(Stage.EVENT_STATION_SUBSETTER, s));
                statii.add(Status.get(Stage.REQUEST_SUBSETTER, s));
            }else if(s.equals(Standing.RETRY)){
                statii.add(Status.get(Stage.AVAILABLE_DATA_SUBSETTER, Standing.REJECT));
                statii.add(Status.get(Stage.DATA_SUBSETTER, s));
            }
        }

        public String getResult(EventAccessOperations ev) {
            int count = 0;
            synchronized(statusTable){
                Iterator it = statii.iterator();
                while(it.hasNext()){
                    try {
                        count += statusTable.getAll(ev, (Status)it.next()).length;
                    } catch (Exception e) { GlobalExceptionHandler.handle(e); }
                }
            }
            return "" + count;
        }

        private List statii = new ArrayList();
    }

    private static JDBCEventChannelStatus statusTable;

    static{
        try {
            statusTable = new JDBCEventChannelStatus();
        } catch (SQLException e) {
            GlobalExceptionHandler.handle(e);
        }
    }

    private String format(double d){
        return defaultDecimalFormat.format(d);
    }

    private  DecimalFormat defaultDecimalFormat = new DecimalFormat("#.#");

    private class RegionName implements EventTemplate{
        public String getResult(EventAccessOperations ev){
            return getRegionName(ev);
        }
    }

    private class Time implements EventTemplate{
        public Time(){this("yyyyMMdd'T'HH:mm:ss.SSS");}

        public Time(String format){
            sdf = new SimpleDateFormat(format);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        }

        public String getResult(EventAccessOperations ev){
            try{
                return sdf.format(new MicroSecondDate(getOrigin(ev).origin_time));
            }catch(NumberFormatException e){
                System.out.println("Offending date_time: " + getOrigin(ev).origin_time.date_time);
                throw e;
            }
        }

        private SimpleDateFormat sdf;
    }

    private class EventStatusFormatter implements EventTemplate {
        public String getResult(EventAccessOperations e){
            if (e instanceof StatefulEvent) {
                return ""+((StatefulEvent)e).getStatus();
            } else {
                return "unknown";
            }
        }
    }

    public void useDefaultConfig() {
        templates.add(new RegionName());
        templates.add(new Time());
    }

    public synchronized String getResult(EventAccessOperations event) {
        StringBuffer name = new StringBuffer();
        Iterator it = templates.iterator();
        while(it.hasNext()){
            name.append(((EventTemplate)it.next()).getResult(event));
        }
        if(filizeResults) return filize(name.toString());
        return name.toString();
    }

    public String getFilizedName(EventAccessOperations event) {
        return filize(getResult(event));
    }

    public static String filize(String fileName){
        fileName = fileName.trim();
        fileName = fileName.replaceAll("[ :]", "_");
        fileName = fileName.replaceAll("[\t\n\f\r]", "");
        return fileName.trim();
    }

    private String getMag(EventAccessOperations event){
        Magnitude[] mags = getOrigin(event).magnitudes;
        if (mags.length > 0){return MagnitudeUtil.toString(mags[0]); }
        throw new IllegalArgumentException("No magnitudes on event");
    }

    private static Origin getOrigin(EventAccessOperations event){
        return CacheEvent.extractOrigin(event);
    }

    public static String getRegionName(EventAccessOperations event){
        return regions.getRegionName(event.get_attributes().region);
    }

    private boolean filizeResults;

    static ParseRegions regions = ParseRegions.getInstance();

    private static Logger logger = Logger.getLogger(EventFormatter.class);
} // NameGenerator

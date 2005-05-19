package edu.sc.seis.sod.status;


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.event.MagnitudeUtil;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
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

public class EventFormatter extends Template implements EventTemplate{

    public EventFormatter(boolean filize) throws ConfigurationException   {
        this(null, filize);
    }

    public EventFormatter(Element config) throws ConfigurationException  {
        this(config, false);
    }

    public EventFormatter(Element config, boolean filize) throws ConfigurationException {
        if(config == null || config.hasChildNodes() == false) useDefaultConfig();
        else parse(config, filize);
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
            return new MagnitudeTemplate();
        } else if(tag.equals("allMagnitudes")) {
            return new EventTemplate(){
                public String getResult(EventAccessOperations ev) {
                    return getMags(ev);
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
            if(s.equals(Standing.SUCCESS)){ stmt = success; }
            else if(s.equals(Standing.REJECT)){ stmt = failed; }
            else if(s.equals(Standing.RETRY)){ stmt = retry; }
        }

        public String getResult(EventAccessOperations ev) {
            int count = 0;
            synchronized(evStatus){
                try {
                    count = evStatus.getNum(stmt, ev);
                } catch (Exception e) { GlobalExceptionHandler.handle(e); }
            }
            return "" + count;
        }

        private PreparedStatement stmt;
    }

    private static JDBCEventChannelStatus evStatus;
    private static PreparedStatement retry, failed, success;

    static{
        try {
            evStatus = new JDBCEventChannelStatus();
            String baseStatement = "SELECT COUNT(*) FROM eventchannelstatus WHERE " +
                "eventid = ?";
            int pass = Status.get(Stage.PROCESSOR, Standing.SUCCESS).getAsShort();
            success = evStatus.prepareStatement(baseStatement + " AND status = " + pass);
            String failReq = JDBCEventChannelStatus.getFailedStatusRequest();
            failed = evStatus.prepareStatement(baseStatement + " AND " + failReq);
            String retryReq = JDBCEventChannelStatus.getRetryStatusRequest();
            retry = evStatus.prepareStatement(baseStatement + " AND " + retryReq);
        } catch (SQLException e) {
            GlobalExceptionHandler.handle(e);
        }
    }

    private String format(double d){
        return defaultDecimalFormat.format(d);
    }

    private  DecimalFormat defaultDecimalFormat = new DecimalFormat("#.#");

    private class MagnitudeTemplate implements EventTemplate {
        public String getResult(EventAccessOperations ev) {
            return getMag(ev);
        }
    };
    
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
                throw new RuntimeException("Offending date_time: " + getOrigin(ev).origin_time.date_time, e);
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
        templates.add(textTemplate("_"));
        templates.add(new Time());
    }

    public synchronized String getResult(EventAccessOperations event) {
        StringBuffer name = new StringBuffer();
        Iterator it = templates.iterator();
        while(it.hasNext()){
            name.append(((EventTemplate)it.next()).getResult(event));
        }
        if(filizeResults) {
            return FissuresFormatter.filize(name.toString());
        } else {
            return name.toString();
        }
    }

    public String getFilizedName(EventAccessOperations event) {
        return FissuresFormatter.filize(getResult(event));
    }

    private String getMags(EventAccessOperations event){
        Magnitude[] mags = getOrigin(event).magnitudes;
        String result = new String();
        for (int i = 0; i < mags.length; i++) {
            result += MagnitudeUtil.toString(mags[i]) + " ";
        }
        return result;
    }

    public static String getMag(EventAccessOperations event){
        Magnitude[] mags = getOrigin(event).magnitudes;
        if (mags.length > 0){return MagnitudeUtil.toString(mags[0]); }
        throw new IllegalArgumentException("No magnitudes on event");
    }

    public static Origin getOrigin(EventAccessOperations event){
        return EventUtil.extractOrigin(event);
    }

    public static String getRegionName(EventAccessOperations event){
        return regions.getRegionName(event.get_attributes().region);
    }

    private boolean filizeResults;

    static ParseRegions regions = ParseRegions.getInstance();

    private static Logger logger = Logger.getLogger(EventFormatter.class);
} // NameGenerator

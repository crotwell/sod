package edu.sc.seis.sod.status;


import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.eventArm.EventTemplate;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class EventFormatter extends Template implements EventTemplate{

    public EventFormatter(){ this(null, false); }

    public EventFormatter(boolean filize){ this(null, filize); }

    public EventFormatter(Element config) {
        this(config, false);
    }

    public EventFormatter(Element config, boolean filize){
        if(config == null || config.hasChildNodes() == false) useDefaultConfig();
        else parse(config);
        filizeResults = filize;
    }

    public static String getDefaultResult(EventAccessOperations event) {
        if(defaultFormatter == null) defaultFormatter = new EventFormatter();
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
                    return format(getOrigin(ev).my_location.depth.value);
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
        }
        return super.getTemplate(tag, el);
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
        if (mags.length > 0)  return format(mags[0].value);
        throw new IllegalArgumentException("No magnitudes on event");
    }

    private static Origin getOrigin(EventAccessOperations event){
        Origin o = event.get_origins()[0];
        try{ o = event.get_preferred_origin(); }catch(NoPreferredOrigin e){}
        return o;
    }

    public static String getRegionName(EventAccessOperations event){
        return regions.getRegionName(event.get_attributes().region);
    }

    private boolean filizeResults;

    static ParseRegions regions = ParseRegions.getInstance();

    private static Logger logger = Logger.getLogger(EventFormatter.class);
} // NameGenerator

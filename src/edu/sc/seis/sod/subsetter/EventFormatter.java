package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.eventArm.EventTemplate;
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
        super(config);
        filizeResults = filize;
    }
    
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
                    return Integer.toString((int)getOrigin(ev).my_location.depth.value);
                }
            };
        } else if(tag.equals("latitude")) {
            return new EventTemplate(){
                public String getResult(EventAccessOperations ev){
                    return Integer.toString((int)getOrigin(ev).my_location.latitude);
                }
            };
        } else if(tag.equals("longitude")) {
            return new EventTemplate(){public String getResult(EventAccessOperations ev){
                    return Integer.toString((int)getOrigin(ev).my_location.longitude);
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
        return null;
    }
    
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
            return sdf.format(new MicroSecondDate(getOrigin(ev).origin_time));
        }
        
        private SimpleDateFormat sdf;
    }
    
    public void useDefaultConfig() {
        templates.add(new RegionName());
        templates.add(new Time());
    }
    
    public String getResult(EventAccessOperations event) {
        StringBuffer name = new StringBuffer();
        Iterator it = templates.iterator();
        while(it.hasNext()){
            name.append(((EventTemplate)it.next()).getResult(event));
        }
        return name.toString();
    }
    
    public String getFilizedName(EventAccessOperations event) {
        return filize(getResult(event));
    }
    
    public static String filize(String fileName){
        fileName = fileName.replaceAll("[/ :]", "_");
        return fileName.replaceAll("[\t\n\f\r]", "");
    }
    
    private static String getMag(EventAccessOperations event){
        Magnitude[] mags = getOrigin(event).magnitudes;
        if (mags.length > 0)  return "" + (int)mags[0].value;
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
    
    static ParseRegions regions = new ParseRegions();
    
    private static Logger logger = Logger.getLogger(EventFormatter.class);
} // NameGenerator

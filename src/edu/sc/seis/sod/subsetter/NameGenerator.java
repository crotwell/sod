package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.sod.SodUtil;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class NameGenerator {
    public NameGenerator(){ this(null); }
    
    public NameGenerator(Element config) {
        this.config = config;
        regions = new ParseRegions();
    }
    
    public String getName(EventAccessOperations event) {
        if(config == null) return getDefaultName(event);
        NodeList children = config.getChildNodes();
        Node node;
        StringBuffer name = new StringBuffer();
        for(int counter = 0; counter < children.getLength(); counter++ ) {
            node = children.item(counter);
            if (node instanceof Text) {
                char[] chars = ((Text)node).getData().toCharArray();
                for (int i=0; i<chars.length; i++) {
                    if (!Character.isWhitespace(chars[i]))name.append(chars[i]);
                } // end of for (int i=0; i<chars.length; i++)
            } else if(node instanceof Element ) {
                Element el = (Element)node;
                if(el.getTagName().equals("feRegionName")) {
                    name.append(getRegionName(event));
                } else if(el.getTagName().equals("feRegionNumber")) {
                    name.append(getRegionNum(event));
                } else if(el.getTagName().equals("depth")) {
                    name.append(getDepth(event));
                } else if(el.getTagName().equals("latitude")) {
                    name.append(getLatitude(event));
                } else if(el.getTagName().equals("longitude")) {
                    name.append(getLongitude(event));
                } else if(el.getTagName().equals("magnitude")) {
                    name.append(getMag(event));
                } else if(el.getTagName().equals("originTime")) {
                    name.append(getTime(event, SodUtil.getText(el)));
                } else {
                    logger.warn("label tag "+el.getTagName()+" is not understood.");
                } // end of else
            }
        }
        return name.toString();
    }
    
    public String getFilizedName(EventAccessOperations event) {
        return filize(getName(event));
    }
    
    public static String filize(String fileName){
        fileName = fileName.replaceAll("[/ :]", "_");
        return fileName.replaceAll("[\t\n\f\r]", "");
    }
    
    public String getDefaultName(EventAccessOperations event){
        String eventFileName =
            regions.getRegionName(event.get_attributes().region);
        eventFileName += " " + getOrigin(event).origin_time.date_time;
        return eventFileName;
    }
    
    private String getTime(EventAccessOperations event, String formatStr) {
        if (formatStr.length() == 0) formatStr = "yyyyMMdd'T'HHmmss.SSS";
        SimpleDateFormat labelFormat = new SimpleDateFormat(formatStr);
        labelFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        MicroSecondDate msd = new MicroSecondDate(getOrigin(event).origin_time);
        return labelFormat.format(msd);
    }
    
    private int getLongitude(EventAccessOperations event) {
        return (int)getOrigin(event).my_location.longitude;
    }
    
    private int getLatitude(EventAccessOperations event) {
        return (int)getOrigin(event).my_location.latitude;
    }
    
    private int getDepth(EventAccessOperations event) {
        return (int)getOrigin(event).my_location.depth.value;
    }
    
    private int getRegionNum(EventAccessOperations event) {
        return event.get_attributes().region.number;
    }
    
    private String getMag(EventAccessOperations event){
        Magnitude[] mags = getOrigin(event).magnitudes;
        if (mags.length > 0)  return "" + (int)mags[0].value;
        throw new IllegalArgumentException("No magnitudes on event");
    }
    
    private Origin getOrigin(EventAccessOperations event){
        Origin o = event.get_origins()[0];
        try{ o = event.get_preferred_origin(); }catch(NoPreferredOrigin e){}
        return o;
    }
    
    private String getRegionName(EventAccessOperations event){
        return regions.getRegionName(event.get_attributes().region);
    }
    
    Element config;
    
    ParseRegions regions;
    
    private static Logger logger = Logger.getLogger(NameGenerator.class);
} // NameGenerator

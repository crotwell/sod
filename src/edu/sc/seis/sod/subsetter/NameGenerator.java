package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.sod.SodUtil;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NameGenerator {
    public NameGenerator(){ this(null); }
    
    public NameGenerator(Element config) {
        parse(config);
    }
    
    private void parse(Element conf){
        if(conf == null) createDefaultBuilder();
        else{
            NodeList children = conf.getChildNodes();
            for(int counter = 0; counter < children.getLength(); counter++ ) {
                Node n = children.item(counter);
                if(n.getNodeType() == Node.TEXT_NODE){
                    builder.add(new Text(n.getNodeValue()));
                } else if(n instanceof Element ) {
                    String tag = ((Element)n).getTagName();
                    if(tag.equals("feRegionName")) {
                        builder.add(new RegionName());
                    } else if(tag.equals("feRegionNumber")) {
                        builder.add(new RegionNum());
                    } else if(tag.equals("depth")) {
                        builder.add(new Depth());
                    } else if(tag.equals("latitude")) {
                        builder.add(new Latitude());
                    } else if(tag.equals("longitude")) {
                        builder.add(new Longitude());
                    } else if(tag.equals("magnitude")) {
                        builder.add(new Mag());
                    } else if(tag.equals("originTime")) {
                        builder.add(new Time(SodUtil.getText((Element)n)));
                    } else {
                        if(n.getChildNodes().getLength() == 0) {
                            builder.add(new Text("<" + n.getNodeName() + getAttrString(n) + "/>"));
                        }else{
                            builder.add(new Text("<" + n.getNodeName()+ getAttrString(n) + ">"));
                            parse((Element)n);
                            builder.add(new Text("</" + n.getNodeName() + ">"));
                        }
                    }
                }
            }
        }
    }
    
    private void createDefaultBuilder() {
        builder.add(new RegionName());
        builder.add(new Time());
    }
    
    private String getAttrString(Node n){
        String result = "";
        NamedNodeMap attr = n.getAttributes();
        for (int i = 0; i < attr.getLength(); i++) {
            result += " " + attr.item(i).getNodeName();
            result += "=\"" + attr.item(i).getNodeValue() + "\"";
        }
        return result;
    }
    
    public String getName(EventAccessOperations event) {
        StringBuffer name = new StringBuffer();
        Iterator it = builder.iterator();
        while(it.hasNext()){
            name.append(((Piece)it.next()).getPiece(event));
        }
        return name.toString();
    }
    
    private interface Piece{
        public String getPiece(EventAccessOperations event);
    }
    
    private class Text implements Piece{
        public Text(String piece){ this.piece = piece; }
        
        public String getPiece(EventAccessOperations event){ return piece; }
        
        private String piece;
    }
    
    private class RegionName implements Piece{
        public String getPiece(EventAccessOperations ev){
            return getRegionName(ev);
        }
    }
    
    private class Depth implements Piece{
        public String getPiece(EventAccessOperations ev){ return
                Integer.toString(getDepth(ev));
        }
    }
    
    private class RegionNum implements Piece{
        public String getPiece(EventAccessOperations ev){
            return Integer.toString(getRegionNum(ev));
        }
    }
    
    private class Latitude implements Piece{
        public String getPiece(EventAccessOperations ev){
            return Integer.toString(getLatitude(ev));
        }
    }
    
    private class Longitude implements Piece{
        public String getPiece(EventAccessOperations ev){
            return Integer.toString(getLongitude(ev));
        }
    }
    
    private class Mag implements Piece{
        public String getPiece(EventAccessOperations ev){ return getMag(ev); }
    }
    
    private class Time implements Piece{
        public Time(){this("yyyyMMdd'T'HH:mm:ss.SSS");}
        
        public Time(String format){
            sdf = new SimpleDateFormat(format);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        
        public String getPiece(EventAccessOperations ev){
            return sdf.format(new MicroSecondDate(getOrigin(ev).origin_time));
        }
        
        private SimpleDateFormat sdf;
    }
    
    public String getFilizedName(EventAccessOperations event) {
        return filize(getName(event));
    }
    
    public static String filize(String fileName){
        fileName = fileName.replaceAll("[/ :]", "_");
        return fileName.replaceAll("[\t\n\f\r]", "");
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
    
    public static String getRegionName(EventAccessOperations event){
        return regions.getRegionName(event.get_attributes().region);
    }
    
    private List builder = new ArrayList();
    
    static ParseRegions regions = new ParseRegions();
    
    private static Logger logger = Logger.getLogger(NameGenerator.class);
} // NameGenerator

package edu.sc.seis.sod.subsetter;


import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.Template;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import org.w3c.dom.Element;

public class ChannelFormatter extends Template implements ChannelTemplate{
    public ChannelFormatter(Element el){ this(el, null); }
    
    public ChannelFormatter(Element el, ChannelGroupTemplate cgt){
        this.cgt = cgt;
        parse(el);
    }
    
    protected Object textTemplate(final String text) {
        return new ChannelTemplate(){
            public String getResult(Channel chan) {
                return text;
            }
        };
    }
    
    protected Object getTemplate(String tag, Element el) {
        if(tag.equals("stationCode")){
            return new ChannelTemplate(){
                public String getResult(Channel chan) {
                    return chan.get_id().station_code;
                }
            };
        }
        else if (tag.equals("channelCode")){
            return new ChannelTemplate(){
                public String getResult(Channel chan){
                    return chan.get_id().channel_code;
                }
            };
        }
        else if(tag.equals("networkCode")){
            return new ChannelTemplate(){
                public String getResult(Channel chan) {
                    return chan.get_id().network_id.network_code;
                }
            };
        }else if(tag.equals("stationCode")){
            return new ChannelTemplate(){
                public String getResult(Channel chan) {
                    return chan.get_code();
                }
            };
        }else if(tag.equals("siteCode")){
            return new ChannelTemplate(){
                public String getResult(Channel chan) {
                    return chan.get_id().site_code;
                }
            };
        }else if(tag.equals("beginTime")) return new ChannelBeginTimeTemplate(el);
        else if(tag.equals("beginTimeUnformatted")){
            return new ChannelTemplate(){
                public String getResult(Channel chan) {
                    return chan.get_id().begin_time.date_time;
                }
            };
        }
        else if(tag.equals("dip")){
            return new ChannelTemplate(){
                public String getResult(Channel chan) {
                    return format(chan.an_orientation.dip);
                }
            };
        }else if(tag.equals("azimuth")){
            return new ChannelTemplate(){
                public String getResult(Channel chan) {
                    return format(chan.an_orientation.azimuth);
                }
            };
        }else if(tag.equals("name")){
            return new ChannelTemplate(){
                public String getResult(Channel chan) {
                    return chan.name;
                }
            };
        }else if(tag.equals("lat")){
            return new ChannelTemplate(){
                public String getResult(Channel chan){
                    return format(chan.my_site.my_location.latitude);
                }
            };
        }else if(tag.equals("lon")){
            return new ChannelTemplate(){
                public String getResult(Channel chan){
                    return format(chan.my_site.my_location.longitude);
                }
            };
        }else if(tag.equals("lon")){
            return new ChannelTemplate(){
                public String getResult(Channel chan){
                    return format(chan.my_site.my_location.longitude);
                }
            };
        }else if(tag.equals("status") && cgt != null){
            return new ChannelTemplate(){
                public String getResult(Channel chan) {
                    return cgt.getStatus(chan);
                }
            };
        }
        return null;
    }
    
    private String format(double d){
        synchronized(formatter){return formatter.format(d); }
    }
    
    private DecimalFormat formatter = new DecimalFormat("#.#");
    
    public String getResult(Channel chan) {
        StringBuffer buf = new StringBuffer();
        Iterator it = templates.iterator();
        while(it.hasNext()){
            ChannelTemplate cur = (ChannelTemplate)it.next();
            buf.append(cur.getResult(chan));
        }
        return buf.toString();
    }
    
    private class ChannelBeginTimeTemplate extends BeginTimeTemplate implements ChannelTemplate{
        
        public ChannelBeginTimeTemplate(Element config){
            super(config);
        }
        
        public String getResult(Channel chan){
            setTime(chan.get_id().begin_time);
            return getResult();
        }
    }
    
    ChannelGroupTemplate cgt;
}

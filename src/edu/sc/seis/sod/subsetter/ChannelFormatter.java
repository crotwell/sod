package edu.sc.seis.sod.subsetter;


import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.Template;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import org.w3c.dom.Element;

public class ChannelFormatter extends Template implements ChannelTemplate{
    public ChannelFormatter(Element el){ parse(el); }
    
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
        }else if(tag.equals("networkCode")){
            return new ChannelTemplate(){
                public String getResult(Channel chan) {
                    return chan.get_id().network_id.network_code;
                }
            };
        }else if(tag.equals("channelCode")){
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
        }else if(tag.equals("beginTime")) return new BeginTimeTemplate(el);
        else if(tag.equals("dip")){
            return new ChannelTemplate(){
                public String getResult(Channel chan) {
                    return Float.toString(chan.an_orientation.dip);
                }
            };
        }else if(tag.equals("azimuth")){
            return new ChannelTemplate(){
                public String getResult(Channel chan) {
                    return Float.toString(chan.an_orientation.azimuth);
                }
            };
        }else if(tag.equals("name")){
            return new ChannelTemplate(){
                public String getResult(Channel chan) {
                    return chan.name;
                }
            };
        }
        return null;
    }
    
    private class BeginTimeTemplate implements ChannelTemplate{
        public BeginTimeTemplate(Element config){
            sdf = new SimpleDateFormat(SodUtil.getNestedText(config));
        }
        
        public String getResult(Channel chan) {
            return sdf.format(new MicroSecondDate(chan.get_id().begin_time));
        }
        
        SimpleDateFormat sdf;
    }
    
    public String getResult(Channel chan) {
        StringBuffer buf = new StringBuffer();
        Iterator it = templates.iterator();
        while(it.hasNext()){
            ChannelTemplate cur = (ChannelTemplate)it.next();
            buf.append(cur.getResult(chan));
        }
        return buf.toString();
    }
}

/**
 * StationFormatter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;



import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.Time;
import java.util.Iterator;
import org.w3c.dom.Element;

public class StationFormatter extends Template implements StationTemplate{
    StationGroupTemplate sgt;
    
    public StationFormatter(Element el){
        this(el, null);
    }
    
    public StationFormatter(Element el, StationGroupTemplate sgt){
        this.sgt = sgt;
        parse(el);
    }
    
    /**
     * Method getResult
     *
     * @param    station             a  Station
     *
     * @return   a String
     *
     */
    public String getResult(Station station) {
        StringBuffer buf = new StringBuffer();
        Iterator it = templates.iterator();
        while(it.hasNext()){
            StationTemplate cur = (StationTemplate)it.next();
            buf.append(cur.getResult(station));
        }
        
        return buf.toString();
    }
    
    
    /**
     * Method textTemplate
     *
     * @param    text                a  String
     *
     * @return   an Object
     *
     */
    protected Object textTemplate(final String text) {
        return new StationTemplate(){
            public String getResult(Station station){
                return text;
            }
        };
    }
    
    /**if this class has an template for this tag, it creates it using the
     * passed in element and returns it.  Otherwise it returns null.
     */
    protected Object getTemplate(String tag, final Element el) {
        if (tag.equals("name")){
            return new StationTemplate(){
                public String getResult(Station sta){
                    return sta.name;
                }
            };
        }
        else if (tag.equals("networkCode")){
            return new StationTemplate(){
                public String getResult(Station sta){
                    return sta.my_network.get_code();
                }
            };
        }
        else if (tag.equals("stationCode")){
            return new StationTemplate(){
                public String getResult(Station sta){
                    return sta.get_code();
                }
            };
        }
        else if (tag.equals("lon")){
            return new StationTemplate(){
                public String getResult(Station sta){
                    return Float.toString(sta.my_location.longitude);
                }
            };
        }
        else if (tag.equals("lat")){
            return new StationTemplate(){
                public String getResult(Station sta){
                    return Float.toString(sta.my_location.latitude);
                }
            };
        }
        else if (tag.equals("depth")){
            return new StationTemplate(){
                public String getResult(Station sta){
                    return Double.toString(sta.my_location.depth.value);
                }
            };
        }
        else if (tag.equals("elevation")){
            return new StationTemplate(){
                public String getResult(Station sta){
                    return Double.toString(sta.my_location.elevation.value);
                }
            };
        }
        else if (tag.equals("comment")){
            return new StationTemplate(){
                public String getResult(Station sta){
                    return sta.comment;
                }
            };
        }
        else if (tag.equals("description")){
            return new StationTemplate(){
                public String getResult(Station sta){
                    return sta.description;
                }
            };
        }
        else if (tag.equals("operator")){
            return new StationTemplate(){
                public String getResult(Station sta){
                    return sta.operator;
                }
            };
        }
        else if (tag.equals("beginTime")){
            return new StationTemplate(){
                public String getResult(Station sta){
                    return btt.getResult(sta.get_id().begin_time);
                }
                TimeTemplate btt = new TimeTemplate(el, false);
            };
        }
        else if (tag.equals("endTime")){
            return new StationTemplate(){
                public String getResult(Station sta){
                    return btt.getResult(sta.effective_time.end_time);
                }
                TimeTemplate btt = new TimeTemplate(el, false);
            };
        }
        else if (tag.equals("beginTimeUnformatted")){
            return new StationTemplate(){
                public String getResult(Station sta){
                    return sta.get_id().begin_time.date_time;
                }
            };
        }
        else if (tag.equals("status") && sgt != null){
            return new StationTemplate(){
                public String getResult(Station sta){
                    return sgt.stationMap.get(sta).toString();
                }
            };
        }
        
        return super.getTemplate(tag, el);
    }

}


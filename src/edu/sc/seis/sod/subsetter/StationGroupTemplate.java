/**
 * StationGroupTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.RunStatus;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.w3c.dom.Element;



public class StationGroupTemplate extends Template implements GenericTemplate{
    Map stationMap = new HashMap();
    
    public StationGroupTemplate(Element el){
        parse(el);
    }
    
    /**if this class has an template for this tag, it creates it using the
     * passed in element and returns it.  Otherwise it returns null.
     */
    protected Object getTemplate(String tag, Element el) {
        if (tag.equals("station")) return new StationFormatter(el, this);
        return null;
    }
    
    /**
     *returns an object of the template type that this class uses, and returns
     * the passed in text when the getResult method of that template type is
     * called
     */
    protected Object textTemplate(final String text) {
        return new StationTemplate(){
            public String getResult(Station sta){
                return text;
            }
        };
    }
    
    public String getResult() {
        StringBuffer buf = new StringBuffer();
        Iterator it = stationMap.keySet().iterator();
        while(it.hasNext()){
            Station cur = (Station)it.next();
            Iterator templateIt = templates.iterator();
            while(templateIt.hasNext()){
                buf.append(((StationTemplate)templateIt.next()).getResult(cur));
            }
        }
        return buf.toString();
    }
    
    public void change(Station sta, RunStatus status){
        stationMap.put(sta, status);
    }
}


/**
 * StationGroupTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;

import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;



public class StationGroupTemplate extends Template implements GenericTemplate{
    Map stationMap = new HashMap();

    public StationGroupTemplate(Element el) throws ConfigurationException {
        parse(el);
    }

    /**if this class has an template for this tag, it creates it using the
     * passed in element and returns it.  Otherwise it returns null.
     */
    protected Object getTemplate(String tag, Element el)  throws ConfigurationException {
        if (tag.equals("station")){ return new StationFormatter(el, this);}
        else if(tag.equals("statusFilter")){
            NodeList nl = el.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i) instanceof Element) {
                    Element child = (Element)nl.item(i);
                    String name = SodUtil.getNestedText(child);
                    try {
                        if (child.getTagName().equals("status")) {
                            useStanding.add(Standing.getForName(name));
                        } else if (child.getTagName().equals("notStatus")) {
                            notUseStanding.add(Standing.getForName(name));
                        }
                    } catch (NoSuchFieldException e) {
                        // this means the config file is wrong, the name is not
                        // a valid Standing...
                        String msg = "status tag "+name+" is not a valid Standing, please use one of: ";

                        Field[] fields = Standing.class.getFields();
                        for (int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++) {
                            if (fields[fieldIndex].getType().equals(Standing.class)) {
                                msg+=((fieldIndex==0)?" ":", ")+fields[fieldIndex].getName();
                            }
                        }
                        throw new ConfigurationException(msg);
                    }
                }
            }
            return new AllTextTemplate("");
        }
        return getCommonTemplate(tag, el);
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
            Status status = (Status)stationMap.get(cur);
            if ((useStanding.size()==0 && ! notUseStanding.contains(status.getStanding()))
                || useStanding.contains(status.getStanding())) {
                Iterator templateIt = templates.iterator();
                while(templateIt.hasNext()){
                    buf.append(((StationTemplate)templateIt.next()).getResult(cur));
                }
            } // don't do anything otherwise
        }
        return buf.toString();
    }

    public void change(Station sta, Status status){
        stationMap.put(sta, status);
    }

    LinkedList useStanding = new LinkedList();
    LinkedList notUseStanding = new LinkedList();
}


/**
 * NetworkGroupTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;


import edu.iris.Fissures.IfNetwork.NetworkAccess;
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



public class NetworkGroupTemplate extends Template implements GenericTemplate {

    Map networkMap = new HashMap();

    public NetworkGroupTemplate(Element el) throws ConfigurationException {
        parse(el);
    }

    /**if this class has an template for this tag, it creates it using the
     * passed in element and returns it.  Otherwise it returns null.
     */
    protected Object getTemplate(String tag, Element element)  throws ConfigurationException {
        if (tag.equals("network")){
            return new NetworkFormatter(element, this);
        } else if (tag.equals("statusFilter")) {
            NodeList nl = element.getChildNodes();
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
        return getCommonTemplate(tag, element);
    }

    /**
     *returns an object of the template type that this class uses, and returns
     * the passed in text when the getResult method of that template type is
     * called
     */
    protected Object textTemplate(final String text) {
        return new NetworkTemplate(){
            public String getResult(NetworkAccess net){
                return text;
            }
        };
    }

    public String getResult() {
        StringBuffer buf = new StringBuffer();
        synchronized(networkMap){
            Iterator it = networkMap.keySet().iterator();
            while (it.hasNext()){
                NetworkAccess cur = (NetworkAccess)it.next();
                Status status = (Status)networkMap.get(cur);
                if ((useStanding.size()==0 && ! notUseStanding.contains(status.getStanding()))
                    || useStanding.contains(status.getStanding())) {
                    Iterator tempIt = templates.iterator();
                    while (tempIt.hasNext()){
                        buf.append(((NetworkTemplate)tempIt.next()).getResult(cur));
                    }
                }
            }
        }
        return buf.toString();
    }

    public void change(NetworkAccess net, Status status){
        synchronized(networkMap){
            networkMap.put(net, status);
        }
    }

    LinkedList useStanding = new LinkedList();
    LinkedList notUseStanding = new LinkedList();

}


/**
 * NetworkGroupTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;


import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.sod.Status;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.w3c.dom.Element;



public class NetworkGroupTemplate extends Template implements GenericTemplate {

    Map networkMap = new HashMap();

    public NetworkGroupTemplate(Element el){
        parse(el);
    }

    /**if this class has an template for this tag, it creates it using the
     * passed in element and returns it.  Otherwise it returns null.
     */
    protected Object getTemplate(String tag, Element el) {
        if (tag.equals("network")){
            return new NetworkFormatter(el, this);
        }
        return super.getTemplate(tag, el);
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
        Iterator it = networkMap.keySet().iterator();
        synchronized(networkMap){
            while (it.hasNext()){
                NetworkAccess cur = (NetworkAccess)it.next();
                Iterator tempIt = templates.iterator();
                while (tempIt.hasNext()){
                    buf.append(((NetworkTemplate)tempIt.next()).getResult(cur));
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

}


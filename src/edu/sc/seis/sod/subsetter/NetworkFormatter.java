/**
 * NetworkFormatter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import java.util.Iterator;
import org.w3c.dom.Element;



public class NetworkFormatter extends Template implements NetworkTemplate{
    NetworkGroupTemplate ngt;
    
    public NetworkFormatter(Element el){
        this(el, null);
    }
    
    public NetworkFormatter(Element el, NetworkGroupTemplate ngt){
        this.ngt = ngt;
        parse(el);
    }
    
    /**
     * Method getResult
     *
     * @param    network             a  NetworkAccess
     *
     * @return   a String
     *
     */
    public String getResult(NetworkAccess network) {
        StringBuffer buf = new StringBuffer();
        Iterator it = templates.iterator();
        while (it.hasNext()){
            NetworkTemplate cur = (NetworkTemplate)it.next();
            buf.append(cur.getResult(network));
        }
        return buf.toString();
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
    
    /**if this class has an template for this tag, it creates it using the
     * passed in element and returns it.  Otherwise it returns null.
     */
    protected Object getTemplate(String tag, Element el) {
        
        if (tag.equals("code")){
            return new NetworkTemplate(){
                public String getResult(NetworkAccess net){
                    return net.get_attributes().get_code();
                }
            };
        }
        else if (tag.equals("beginTime")){
            return new BeginTimeTemplate(el);
        }
        else if (tag.equals("name")){
            return new NetworkTemplate(){
                public String getResult(NetworkAccess net){
                    return net.get_attributes().name;
                }
            };
        }
        else if (tag.equals("description")){
            return new NetworkTemplate(){
                public String getResult(NetworkAccess net){
                    return net.get_attributes().description;
                }
            };
        }
        else if (tag.equals("owner")){
            return new NetworkTemplate(){
                public String getResult(NetworkAccess net){
                    return net.get_attributes().owner;
                }
            };
        }
        else if (tag.equals("status") && ngt != null){
            return new NetworkTemplate(){
                public String getResult(NetworkAccess net){
                    return ngt.networkMap.get(net).toString();
                }
            };
        }
        
        return null;
    }
    
}


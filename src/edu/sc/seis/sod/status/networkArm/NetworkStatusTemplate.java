/**
 * NetworkStatusTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.networkArm;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.status.GenericTemplate;
import edu.sc.seis.sod.status.NetworkGroupTemplate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class NetworkStatusTemplate extends NetworkInfoTemplate{

    private String status = "";
    private List networkListeners = new ArrayList();
    private Logger logger = Logger.getLogger(NetworkStatusTemplate.class);

    public NetworkStatusTemplate(Element el, String baseDir, String outputLocation) throws IOException{
        super(baseDir, outputLocation);
        parse(el);
        write();
    }

    public void change(NetworkAccess networkAccess, Status status) {
        Iterator it = networkListeners.iterator();
        while (it.hasNext()){
            NetworkGroupTemplate ngt = (NetworkGroupTemplate)it.next();
            ngt.change(networkAccess, status);
        }
        write();
    }

    public void setArmStatus(String status)  {
        this.status = status;
        write();
    }

    private class StatusFormatter implements GenericTemplate{
        public String getResult(){ return status; }
    }

    /**if this class has an template for this tag, it creates it using the
     * passed in element and returns it.  Otherwise it returns null.
     */
    protected Object getTemplate(String tag, Element el) {
        if (tag.equals("networks")) {
            NetworkGroupTemplate t = new NetworkGroupTemplate(el);
            networkListeners.add(t);
            return t;
        }
        else if (tag.equals("status")){
            return new StatusFormatter();
        }
        return super.getTemplate(tag,el);
    }

}



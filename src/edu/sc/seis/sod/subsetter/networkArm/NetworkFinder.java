package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.sc.seis.fissuresUtil.namingService.*;
import edu.iris.Fissures.IfNetwork.*;


import org.w3c.dom.*;
import org.apache.log4j.*;
/**
 * NetworkFinder.java
 *
 *
 * Created: Wed Mar 20 14:27:42 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class NetworkFinder extends AbstractSource{
    /**
     * Creates a new <code>NetworkFinder</code> instance.
     *
     * @param element an <code>Element</code> value
     */
    public NetworkFinder (Element element) throws Exception{
	super(element);
	    CommonAccess commonAccess = CommonAccess.getCommonAccess();
	    fissuresNamingService = commonAccess.getFissuresNamingService();
	    
	    dns = getDNSName();
	    objectName = getSourceName();
	    Element subElement = SodUtil.getElement(element,"refreshInterval");
	    if(subElement != null) {
	    	Object obj = SodUtil.load(subElement, "edu.sc.seis.sod.subsetter");
	    	refreshInterval = (RefreshInterval)obj;
	    } else refreshInterval = null;	
	
    }

    /**
     * Describe <code>getNetworkDC</code> method here.
     *
     * @return a <code>NetworkDC</code> value
     */
    public NetworkDC getNetworkDC() throws Exception{

	    return fissuresNamingService.getNetworkDC(dns, objectName);	

    }

    public RefreshInterval getRefreshInterval() {

	return this.refreshInterval;
    }

    private FissuresNamingServiceImpl fissuresNamingService = null;

    private String dns = null;

    private String objectName = null;

    private RefreshInterval refreshInterval;
        
}// NetworkFinder

package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
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
    public NetworkFinder (Element element){
	super(element);
	try{
	    System.out.println("Now the source NetworkSource must be built");
	    CommonAccess commonAccess = CommonAccess.getCommonAccess();
	    if(commonAccess == null) System.out.println("THe common Acces is null");
	    fissuresNamingService = commonAccess.getFissuresNamingService();
	    
	    if(fissuresNamingService == null) System.out.println("NULLLLL");
	    else System.out.println("NOT NULLLLL");
	    System.out.println("The dns name is "+getDNSName());
	    System.out.println("The object name is "+getSourceName());
	    dns = getDNSName();
	    objectName = getSourceName();

	} catch(Exception e) {
	    
	    e.printStackTrace();
	    
	}
	
    }

    public NetworkDC getNetworkDC() {

	try {
	    return fissuresNamingService.getNetworkDC(dns, objectName);	
	} catch(Exception e) {

	    System.out.println("Caught exception while getting networkDC ");

	}
	return null;

    }

   private FissuresNamingServiceImpl fissuresNamingService = null;

   private String dns = null;

   private String objectName = null;
        
}// NetworkFinder

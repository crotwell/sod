package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;
import edu.sc.seis.fissuresUtil.namingService.*;

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
	try{
	    System.out.println("Now the source NetworkSource must be built");
	    CommonAccess commonAccess = CommonAccess.getCommonAccess();
	    if(commonAccess == null) System.out.println("THe common Acces is null");
	    FissuresNamingServiceImpl fissuresNamingService = commonAccess.getFissuresNamingService();
	    
	    if(fissuresNamingService == null) System.out.println("NULLLLL");
	    else System.out.println("NOT NULLLLL");
	} catch(Exception e) {

	    e.printStackTrace();

	}
	
    }
    
    
    
}// NetworkFinder

package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.fissuresUtil.namingService.*;
import edu.iris.Fissures.IfSeismogramDC.*;


import org.w3c.dom.*;
import org.apache.log4j.*;
/**
 * FixedDataCenter.java
 *
 *
 * Created: Wed Mar 20 14:27:42 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class FixedDataCenter extends AbstractSource implements SodElement{
    /**
     * Creates a new <code>FixedDataCenter</code> instance.
     *
     * @param element an <code>Element</code> value
     */
    public FixedDataCenter (Element element){
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

    /**
     * Describe <code>getSeismogramDC</code> method here.
     *
     * @return a <code>DataCenter</code> value
     */
    public DataCenter getSeismogramDC() {

	try {
	    return fissuresNamingService.getSeismogramDC(dns, objectName);	
	} catch(Exception e) {

	    System.out.println("Caught exception while getting networkDC ");

	}
	return null;

    }

   private FissuresNamingServiceImpl fissuresNamingService = null;

   private String dns = null;

   private String objectName = null;
        
}// FixedDataCenter

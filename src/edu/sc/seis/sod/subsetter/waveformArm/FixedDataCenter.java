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
    public FixedDataCenter (Element element) throws Exception{
	super(element);
	    CommonAccess commonAccess = CommonAccess.getCommonAccess();
	    fissuresNamingService = commonAccess.getFissuresNamingService();
	    
	    dns = getDNSName();
	    objectName = getSourceName();
        dataCenter = fissuresNamingService.getSeismogramDC(dns, objectName);	
    }

    /**
     * Describe <code>getSeismogramDC</code> method here.
     *
     * @return a <code>DataCenter</code> value
     */
    public DataCenter getSeismogramDC() throws Exception{
        return dataCenter;
    }

    private DataCenter dataCenter;

   private FissuresNamingServiceImpl fissuresNamingService = null;

   private String dns = null;

   private String objectName = null;
        
}// FixedDataCenter

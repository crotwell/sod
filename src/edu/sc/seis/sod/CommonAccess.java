package edu.sc.seis.sod;

import edu.sc.seis.fissuresUtil.namingService.*;
import edu.iris.Fissures.model.*;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;

import java.io.*;
import org.apache.log4j.*;


/**
 * CommonAccess.java
 *
 *
 * Created: Wed Mar 20 14:00:32 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class CommonAccess {
    
    private CommonAccess (){
	
	//return commonAccess();
	
    }

    /**
     * Describe <code>getCommonAccess</code> method here.
     *
     * @return a <code>CommonAccess</code> value
     */
    public static CommonAccess getCommonAccess() {

	return commonAccess;

    }

    /**
     * Describe <code>getFissuresNamingService</code> method here.
     *
     * @return a <code>FissuresNamingServiceImpl</code> value
     * @exception Exception if an error occurs
     */
    public FissuresNamingServiceImpl getFissuresNamingService() throws Exception{

	FissuresNamingServiceImpl fissuresNamingService = new FissuresNamingServiceImpl(getORB());
	return fissuresNamingService;

    }

  
    
    /**
     * Describe <code>initORB</code> method here.
     *
     */
    protected void initORB(String[] args, java.util.Properties props) {
	if (orb == null) {
	    // return orb;
	
	
	// Initialize the ORB.
	orb = 
	    (org.omg.CORBA_2_3.ORB)org.omg.CORBA.ORB.init(args, props);
            
	// register valuetype factories
	AllVTFactory vt = new AllVTFactory();
	vt.register(orb);
	}
    }

     
    /**
     * Describe <code>getORB</code> method here.
     *
     * @return an <code>org.omg.CORBA_2_3.ORB</code> value
     * @exception ConfigurationException if an error occurs
     */
    public org.omg.CORBA_2_3.ORB getORB() throws ConfigurationException  {
	if (orb == null) {
	    initORB(null, null);
	} // end of if (orb == null)
	return orb;
    }

    private String[] args;

    
    private static CommonAccess commonAccess = new CommonAccess();

    private org.omg.CORBA_2_3.ORB orb = null;

    static Category logger = 
        Category.getInstance(CommonAccess.class.getName());


    
}// CommonAccess

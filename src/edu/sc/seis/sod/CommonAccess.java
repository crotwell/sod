package edu.sc.seis.sod;

import edu.sc.seis.fissuresUtil.namingService.*;
import edu.iris.Fissures.model.*;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;

import java.io.*;


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
     * Describe <code>init</code> method here.
     *
     * @param args a <code>String[]</code> value
     * @exception ConfigurationException if an error occurs
     */
    protected void init(String[] args) throws ConfigurationException {
	this.args = args;

	props = System.getProperties();

	// get some defaults
	String propFilename=
	    "sod.prop";
	String defaultsFilename=
	    "edu/sc/seis/sod/"+propFilename;
	    
	if((CommonAccess.class).getClassLoader() != null)
	    try {
		props.load((CommonAccess.class).getClassLoader().getResourceAsStream( defaultsFilename ));
	    } catch (Exception e) {
		//logger.warn
		System.err.println("Could not load defaults. "+e);
	    }
	for (int i=0; i<args.length-1; i++) {
	    if (args[i].equals("-props")) {
		// override with values in local directory, 
		// but still load defaults with original name
		propFilename = args[i+1];
		try {
		    FileInputStream in = new FileInputStream(propFilename);
		    props.load(in);	
		    in.close();
		} catch (FileNotFoundException f) {
		    //logger.warn
		    System.err.println(" file missing "+f+" using defaults");
		} catch (IOException f) {
		    //logger.warn
		    System.err.println(f.toString()+" using defaults");
		}
	    }
	}


    }

    
    /**
     * Describe <code>initORB</code> method here.
     *
     */
    protected void initORB() {
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
	    initORB();
	} // end of if (orb == null)
	return orb;
    }

    private String[] args;

    private java.util.Properties props;
    
    private static CommonAccess commonAccess = new CommonAccess();

    private org.omg.CORBA_2_3.ORB orb = null;



    
}// CommonAccess

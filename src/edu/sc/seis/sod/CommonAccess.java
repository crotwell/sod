package edu.sc.seis.sod;

import edu.iris.Fissures.model.AllVTFactory;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingServiceImpl;
import org.apache.log4j.Category;
import org.apache.log4j.lf5.util.LogMonitorAdapter;


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
    
    public static CommonAccess getCommonAccess() { return commonAccess; }
    
    /**
     * Describe <code>getFissuresNamingService</code> method here.
     *
     * @return a <code>FissuresNamingServiceImpl</code> value
     * @exception Exception if an error occurs
     */
    public FissuresNamingServiceImpl getFissuresNamingService() throws Exception{
        if (fissuresNamingService == null) {
            fissuresNamingService = new FissuresNamingServiceImpl(getORB());
            java.util.Properties props = System.getProperties();
            if ( props.containsKey(NAME_SERVICE_PROP)) {
                fissuresNamingService.setNameServiceCorbaLoc((String)props.get(NAME_SERVICE_PROP));
            } // end of if ()
        } // end of if (fissuresNamingService == null)
        
        return fissuresNamingService;
        
    }
    
    
    
    /**
     * Describe <code>initORB</code> method here.
     *
     */
    protected void initORB(String[] args, java.util.Properties props) {
        if (orb == null) {
            // Initialize the ORB.
            orb = (org.omg.CORBA_2_3.ORB)org.omg.CORBA.ORB.init(args, props);
            
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
    
    public LogMonitorAdapter getLF5Adapter(){ return adapter; }
    
    private static CommonAccess commonAccess = new CommonAccess();
    
    
    private LogMonitorAdapter adapter = LogMonitorAdapter.newInstance(LogMonitorAdapter.LOG4J_LOG_LEVELS);
    private org.omg.CORBA_2_3.ORB orb = null;
    
    FissuresNamingServiceImpl fissuresNamingService;
    
    static final String NAME_SERVICE_PROP =
        "edu.sc.seis.sod.nameServiceCorbaLoc";
    
    static Category logger =
        Category.getInstance(CommonAccess.class.getName());
    
    
    
}// CommonAccess

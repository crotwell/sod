package edu.sc.seis.sod;

import edu.iris.Fissures.model.AllVTFactory;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.util.LogMonitorAdapter;
import java.io.IOException;


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
    private CommonAccess (){}

    public static void handleException(Throwable t, String reason) {
        handleException(reason, t);
    }
    public static void handleException(String reason, Throwable t) {
        logger.error(reason, t);
    }

    public static CommonAccess getCommonAccess() { return commonAccess; }

    public FissuresNamingService getFissuresNamingService() throws ConfigurationException {
        if (fissuresNamingService == null) {
            fissuresNamingService = new FissuresNamingService(getORB());
            java.util.Properties props = System.getProperties();
            if ( props.containsKey(NAME_SERVICE_PROP)) {
                fissuresNamingService.setNameServiceCorbaLoc((String)props.get(NAME_SERVICE_PROP));
            } // end of if ()
        } // end of if (fissuresNamingService == null)

        return fissuresNamingService;

    }

    protected void initORB(String[] args, java.util.Properties props) {
        if (orb == null) {
            // Initialize the ORB.
            orb = (org.omg.CORBA_2_3.ORB)org.omg.CORBA.ORB.init(args, props);
            logger.info("ORB class is "+orb.getClass().getName());
            // register valuetype factories
            AllVTFactory vt = new AllVTFactory();
            vt.register(orb);
        }
    }

    public org.omg.CORBA_2_3.ORB getORB() {
        if (orb == null) {
            initORB(null, null);
        } // end of if (orb == null)
        return orb;
    }

    public LogMonitorAdapter getLF5Adapter(){
        if(adapter == null){
            adapter = LogMonitorAdapter.newInstance(RunStatus.getLogLevels());
        }
        return adapter;
    }

    private static CommonAccess commonAccess = new CommonAccess();

    private LogMonitorAdapter adapter;

    private org.omg.CORBA_2_3.ORB orb = null;

    FissuresNamingService fissuresNamingService;

    static final String NAME_SERVICE_PROP =
        "edu.sc.seis.sod.nameServiceCorbaLoc";

    private static Logger logger = Logger.getLogger(CommonAccess.class);

}// CommonAccess


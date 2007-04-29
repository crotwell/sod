package edu.sc.seis.sod;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import org.apache.log4j.Logger;
import edu.iris.Fissures.model.AllVTFactory;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

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

    private static final String NAME_SERVICE_ADDRESS = "/NameService";

    private static final String CORBALOC_DESC = "corbaloc:iiop:";

    private CommonAccess() {}

    public static ClassLoader getLoader() {
        return CommonAccess.class.getClassLoader();
    }

    public static FissuresNamingService getNameService() {
        return ns;
    }

    public static String getNameServiceAddress() {
        return ns.getNameServiceCorbaLoc();
    }

    public static org.omg.CORBA_2_3.ORB getORB() {
        return orb;
    }

    public static synchronized void initialize(Properties props, String[] args)
            throws UserConfigurationException {
        if(orb != null) {
            throw new RuntimeException("Initialize should only be called once on CommonAccess");
        }
        vetNSLoc(props);
        
        // Initialize the ORB.
        orb = (org.omg.CORBA_2_3.ORB)org.omg.CORBA.ORB.init(args, props);
        logger.info("ORB class is " + orb.getClass().getName());
        new AllVTFactory().register(orb);
        
        //Initialize the NS
        ns = new FissuresNamingService(getORB());
        ns.setNameServiceCorbaLoc(nsLoc);
    }

    private static void vetNSLoc(Properties props)
            throws UserConfigurationException {
        if(props.containsKey(FissuresNamingService.CORBALOC_PROP)) {
            nsLoc = (String)props.get(FissuresNamingService.CORBALOC_PROP);
        } else if(System.getProperties()
                .containsKey(FissuresNamingService.CORBALOC_PROP)) {
            nsLoc = (String)System.getProperties()
                    .get(FissuresNamingService.CORBALOC_PROP);
        } else {
            throw new UserConfigurationException(FissuresNamingService.CORBALOC_PROP
                    + " must be set in the properties");
        }
        if(!nsLoc.startsWith(CORBALOC_DESC)
                || !nsLoc.endsWith(NAME_SERVICE_ADDRESS)) {
            throw new UserConfigurationException(FissuresNamingService.CORBALOC_PROP
                    + " must start with "
                    + CORBALOC_DESC
                    + " and end with "
                    + NAME_SERVICE_ADDRESS
                    + " but "
                    + nsLoc
                    + " was supplied in the properties");
        }
    }

    private static org.omg.CORBA_2_3.ORB orb;

    private static FissuresNamingService ns;

    private static String nsLoc;

    private static Logger logger = Logger.getLogger(CommonAccess.class);
}// CommonAccess

package edu.sc.seis.sod;

import java.util.HashSet;
import java.util.Set;

import org.omg.CORBA.TRANSIENT;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import edu.sc.seis.fissuresUtil.cache.BulletproofVestFactory;
import edu.sc.seis.fissuresUtil.cache.ClassicRetryStrategy;
import edu.sc.seis.fissuresUtil.cache.CorbaServerWrapper;
import edu.sc.seis.sod.source.AbstractSource;
import edu.sc.seis.seisFile.fdsnws.FDSNWSException;

public class UserReportRetryStrategy extends ClassicRetryStrategy {

    public UserReportRetryStrategy(int numRetries, String additionalInfo) {
        super(numRetries);
        addlInfo = additionalInfo;
    }

    public UserReportRetryStrategy(int numRetries) {
        this(numRetries, "");
    }

    public UserReportRetryStrategy() {
        this(BulletproofVestFactory.getDefaultNumRetry());
    }

    public synchronized boolean shouldRetry(Throwable exc,
                                            Object server,
                                            int tryCount) {
        String serverId = makeServerId(server);
        if(!bustedServers.contains(serverId)) {
            if(exc instanceof TRANSIENT && exc.getCause() instanceof NotFound) {
                print("SOD was unable to find a "
                        + serverId
                        + " in the name service.  Check\n"
                        + getWilyURL()
                        + "\n for that server to make sure you've put the correct location in your ingredient for that server.  If you're sure it's correct, just wait.  SOD will continue trying to find it until it's readded to the name server at which point an all clear message will be issued.  If you're tired of waiting, press Ctrl-C to quit.  "
                        + addlInfo);
            } else {
                if (Start.getArgs().isQuitOnError()) {

                    print("The "
                          + serverId
                          + " server just produced an error ("+exc.getClass().getName()
                          +").  SOD will quit due to the --"+Start.getArgs().QUIT_ON_ERROR_SWITCH+" switch.  "
                          + addlInfo);
                    System.exit(1);
                } else {
		    String url = "";
		    if (exc instanceof FDSNWSException) {
			    url = ((FDSNWSException)exc).getTargetURI().toString();
		    }
                    print("The "
                          + serverId
                          + " server just produced an error ("+exc.getClass().getName()+" "+exc.getMessage()
                          +").  SOD will continue trying it until it recovers at which point an all clear message will be issued.  If it never recovers, email sod@seis.sc.edu with this report and we can inform the server maintainer.  If you're tired of waiting, press Ctrl-C to quit.  "+url
                          + addlInfo);
                }
            }
            logger.info(serverId+" error ", exc);
            bustedServers.add(serverId);
        }
        return super.shouldRetry(exc, server, tryCount);
    }

    public synchronized void serverRecovered(Object server) {
        String serverId = makeServerId(server);
        if(bustedServers.contains(serverId)) {
            bustedServers.remove(serverId);
            print("All clear!  " + serverId
                    + " just recovered.  Processing will continue normally.");
        }
    }

    private void print(String msg) {
        logger.warn(msg);
    }

    private String makeServerId(Object server) {
        if (server instanceof CorbaServerWrapper) {
        return ((CorbaServerWrapper)server).getFullName() + " " + ((CorbaServerWrapper)server).getServerType();
        } else if (server instanceof AbstractSource) {
            return ((AbstractSource)server).getName();
        } else {
            return server.toString();
        }
    }

    public static String getWilyURL() {
        String nsLoc = CommonAccess.getNameServiceAddress();
        if(nsLoc.equals("corbaloc:iiop:dmc.iris.washington.edu:6371/NameService")) {
            return "http://www.seis.sc.edu/ns/IRIS";
        } else if(nsLoc.equals("corbaloc:iiop:nameservice.seis.sc.edu:6371/NameService")) {
            return "http://www.seis.sc.edu/ns/SC";
        }
        return WILY_NS_URL + nsLoc;
    }

    private Object addlInfo;

    private Set bustedServers = new HashSet();

    public static final String WILY_NS_URL = "http://www.seis.sc.edu/wily/GetAllServers?corbaLoc=";

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserReportRetryStrategy.class);
}

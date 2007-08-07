package edu.sc.seis.sod;

import java.util.HashSet;
import java.util.Set;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSIENT;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import edu.sc.seis.fissuresUtil.cache.ClassicRetryStrategy;
import edu.sc.seis.fissuresUtil.cache.CorbaServerWrapper;

public class UserReportRetryStrategy extends ClassicRetryStrategy {

    public UserReportRetryStrategy(String additionalInfo) {
        addlInfo = additionalInfo;
    }

    public UserReportRetryStrategy() {
        this("");
    }

    public synchronized boolean shouldRetry(SystemException exc,
                                            CorbaServerWrapper server,
                                            int tryCount,
                                            int numRetries) {
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
                print("The "
                        + serverId
                        + " server just produced an error.  SOD will continue trying it until it recovers at which point an all clear message will be issued.  If it never recovers, email sod@seis.sc.edu with this report and we can inform the server maintainer.  If you're tired of waiting, press Ctrl-C to quit.  "
                        + addlInfo);
            }
            logger.debug(serverId+" error ", exc);
            bustedServers.add(serverId);
        }
        return super.shouldRetry(exc, server, tryCount, numRetries);
    }

    public synchronized void serverRecovered(CorbaServerWrapper server) {
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

    private String makeServerId(CorbaServerWrapper server) {
        return server.getFullName() + " " + server.getServerType();
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

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(UserReportRetryStrategy.class);
}

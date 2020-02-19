package edu.sc.seis.sod;

import java.util.HashSet;
import java.util.Set;


import edu.sc.seis.sod.retry.ClassicRetryStrategy;
import edu.sc.seis.sod.source.AbstractSource;

public class UserReportRetryStrategy extends ClassicRetryStrategy {

    public UserReportRetryStrategy(int numRetries, String additionalInfo) {
        super(numRetries);
        addlInfo = additionalInfo;
    }

    public UserReportRetryStrategy(int numRetries) {
        this(numRetries, "");
    }

    public UserReportRetryStrategy() {
        this(getDefaultNumRetry());
    }

    public synchronized boolean shouldRetry(Throwable exc,
                                            Object server,
                                            int tryCount) {
        String serverId = makeServerId(server);
        if(!bustedServers.contains(serverId)) {
                if (Start.getArgs().isQuitOnError()) {
                    print("The "
                          + serverId
                          + " server just produced an error ("+exc.getClass().getName()
                          +").  SOD will quit due to the --"+Args.QUIT_ON_ERROR_SWITCH+" switch.  "
                          + addlInfo);
                    System.exit(1);
                } else {
                    print("The "
                          + serverId
                          + " server just produced an error ("+exc.getClass().getName()+" "+exc.getMessage()
                          +").  SOD will continue trying it until it recovers at which point an all clear message will be issued.  If it never recovers, email sod@seis.sc.edu with this report and we can inform the server maintainer.  If you're tired of waiting, press Ctrl-C to quit.  "
                          + addlInfo);
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
        if (server instanceof AbstractSource) {
            return ((AbstractSource)server).getName();
        } else {
            return server.toString();
        }
    }

    private Object addlInfo;

    private Set<String> bustedServers = new HashSet<String>();

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserReportRetryStrategy.class);
}

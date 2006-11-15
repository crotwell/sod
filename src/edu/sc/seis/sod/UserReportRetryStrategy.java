package edu.sc.seis.sod;

import org.omg.CORBA.SystemException;
import edu.sc.seis.fissuresUtil.cache.ClassicRetryStrategy;
import edu.sc.seis.fissuresUtil.cache.CorbaServerWrapper;

public class UserReportRetryStrategy extends ClassicRetryStrategy {

    public boolean shouldRetry(SystemException exc,
                               CorbaServerWrapper server,
                               int tryCount,
                               int numRetries) {
        logger.warn("The "
                + server.getServerDNS()
                + "/"
                + server.getServerName()
                + " "
                + server.getServerType()
                + " server just produced an error.  SOD will continue trying it until this returns succesfully.  If this continues indefinitely email sod@seis.sc.edu with this report and we can inform the server maintainer");
        return super.shouldRetry(exc, server, tryCount, numRetries);
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(UserReportRetryStrategy.class);
}

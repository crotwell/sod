package edu.sc.seis.sod;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSIENT;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.ClassicRetryStrategy;
import edu.sc.seis.fissuresUtil.cache.CorbaServerWrapper;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;

public class UserReportRetryStrategy extends ClassicRetryStrategy {

    public boolean shouldRetry(SystemException exc,
                               CorbaServerWrapper server,
                               int tryCount,
                               int numRetries) {
        if(lastReport.difference(ClockUtil.now()).greaterThan(TEN_MINUTES)) {
            lastReport = ClockUtil.now();
            String nsLoc = CommonAccess.getCommonAccess()
                    .getFissuresNamingService()
                    .getNameServiceCorbaLoc();
            String fullName = server.getServerDNS() + "/"
                    + server.getServerName();
            String wilyURL = WILY_NS_URL + nsLoc;
            if(exc instanceof TRANSIENT && exc.getCause() instanceof NotFound) {
                logger.warn("SOD was unable to find a "
                        + fullName
                        + " "
                        + server.getServerType()
                        + " in the name service.  Check "
                        + wilyURL
                        + " for that server to make sure you've put the correct location in your ingredient for that server.  If you're sure it's correct, just wait.  SOD will continue trying to find it until it's readded to the name server.");
            } else {
                logger.warn("The "
                        + fullName
                        + " "
                        + server.getServerType()
                        + " server just produced an error.  SOD will continue trying it until this returns succesfully.  If this continues indefinitely email sod@seis.sc.edu with this report and we can inform the server maintainer");
            }
        }
        return super.shouldRetry(exc, server, tryCount, numRetries);
    }

    private MicroSecondDate lastReport = new MicroSecondDate(0);

    private static final TimeInterval TEN_MINUTES = new TimeInterval(10,
                                                                     UnitImpl.MINUTE);

    private String WILY_NS_URL = "http://www.seis.sc.edu/wily/GetAllServers?corbaLoc=";

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(UserReportRetryStrategy.class);
}

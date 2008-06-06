package edu.sc.seis.sod.subsetter.dataCenter;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.HardCodeDataCenterRouter;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.CookieJar;

/**
 * @author groves Created on Apr 12, 2005
 */
public class BestIRISDataCenter implements SeismogramDCLocator {

    public ProxySeismogramDC getSeismogramDC(CacheEvent event,
                                             ChannelImpl channel,
                                             RequestFilter[] infilters,
                                             CookieJar cookieJar) {
        return router.getDataCenter(infilters);
    }

    HardCodeDataCenterRouter router = new HardCodeDataCenterRouter(CommonAccess.getNameService());
}

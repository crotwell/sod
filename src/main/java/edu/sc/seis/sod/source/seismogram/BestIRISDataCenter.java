package edu.sc.seis.sod.source.seismogram;

import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.sod.CookieJar;

/**
 * @author groves Created on Apr 12, 2005
 */
@Deprecated
public class BestIRISDataCenter implements SeismogramSourceLocator {

    public SeismogramSource getSeismogramSource(CacheEvent event,
                                             ChannelImpl channel,
                                             RequestFilter[] infilters,
                                             CookieJar cookieJar) throws Exception {
        MicroSecondDate oTime = event.getPreferred().getTime();
        if (ClockUtil.now().subtract(THIRTY_DAY).before(oTime)) {
            return bud.getSeismogramSource(event, channel , infilters, cookieJar);
        } else  {
            Magnitude usgsMw = null;
            Magnitude[] mags = event.getPreferred().getMagnitudes();
            for (int i = 0; i < mags.length; i++) {
                if (mags[i].contributor.equalsIgnoreCase("usgs") && mags[i].type.equalsIgnoreCase("mw")) {
                    usgsMw = mags[i];
                }
            }
            if (usgsMw != null && usgsMw.value >= 5.7) {
                return pond.getSeismogramSource(event, channel , infilters, cookieJar);
            }
        }
        return everything.getSeismogramSource(event, channel , infilters, cookieJar);
    }

    static final FixedDataCenter bud = new FixedDataCenter("edu/iris/dmc", "IRIS_BudDataCenter");

    static final FixedDataCenter pond = new FixedDataCenter("edu/iris/dmc", "IRIS_PondDataCenter");

    static final FixedDataCenter everything = new FixedDataCenter("edu/iris/dmc", "IRIS_DataCenter");

    static final TimeInterval THIRTY_DAY = new TimeInterval(30, UnitImpl.DAY);
}

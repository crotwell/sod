package edu.sc.seis.sod.subsetter.availableData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class NoGaps implements AvailableDataSubsetter, SodElement {

    public NoGaps(Element config) {}

    public StringTree accept(CacheEvent event,
                             ChannelImpl channel,
                          RequestFilter[] request,
                          RequestFilter[] available,
                          CookieJar cookieJar) {
        boolean ok = true;
        logger.debug("original length=" + request.length
                + "  available legnth=" + available.length);
        for(int counter = 0; counter < request.length; counter++) {
            ok = false;
            MicroSecondDate originalStartDate = new MicroSecondDate(request[counter].start_time);
            MicroSecondDate originalEndDate = new MicroSecondDate(request[counter].end_time);
            for(int subcounter = 0; subcounter < available.length; subcounter++) {
                MicroSecondDate availableStartDate = new MicroSecondDate(available[subcounter].start_time);
                MicroSecondDate availableEndDate = new MicroSecondDate(available[subcounter].end_time);
                logger.debug(originalStartDate + " " + originalEndDate + " - "
                        + availableStartDate + " " + availableEndDate);
                if((originalStartDate.after(availableStartDate) || originalStartDate.equals(availableStartDate))
                        && (originalEndDate.before(availableEndDate) || originalEndDate.equals(availableEndDate))) {
                    ok = true;
                    break;
                }
            }
            if(ok == false) {
                logger.debug("NoGaps fail");
                return new StringTreeLeaf(this, false);
            } // end of if (ok == false)
        }
        return new StringTreeLeaf(this, true);
    }

    private static Logger logger = LoggerFactory.getLogger(NoGaps.class);
}// NoGaps

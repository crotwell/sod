package edu.sc.seis.sod.subsetter.availableData;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class NoGaps implements AvailableDataSubsetter, SodElement {

    public NoGaps(Element config) {}

    public StringTree accept(CacheEvent event,
                             Channel channel,
                          RequestFilter[] request,
                          RequestFilter[] available,
                          CookieJar cookieJar) {
        boolean ok = true;
        logger.debug("original length=" + request.length
                + "  available legnth=" + available.length);
        for(int counter = 0; counter < request.length; counter++) {
            ok = false;
            Instant originalStartDate = request[counter].start_time;
            Instant originalEndDate = request[counter].end_time;
            for(int subcounter = 0; subcounter < available.length; subcounter++) {
                Instant availableStartDate = available[subcounter].start_time;
                Instant availableEndDate = available[subcounter].end_time;
                logger.debug(originalStartDate + " " + originalEndDate + " - "
                        + availableStartDate + " " + availableEndDate);
                if((originalStartDate.isAfter(availableStartDate) || originalStartDate.equals(availableStartDate))
                        && (originalEndDate.isBefore(availableEndDate) || originalEndDate.equals(availableEndDate))) {
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

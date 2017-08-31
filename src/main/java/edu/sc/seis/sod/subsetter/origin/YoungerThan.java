package edu.sc.seis.sod.subsetter.origin;

import java.time.Duration;
import java.time.Instant;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.EventAttrImpl;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.util.time.ClockUtil;

/**
 * @author groves Created on Apr 19, 2005
 */
public class YoungerThan implements OriginSubsetter {

    public YoungerThan(Element config) throws ConfigurationException {
        expirationAge = SodUtil.loadTimeInterval(config);
    }

    public StringTree accept(CacheEvent ev,
                          EventAttrImpl eventAttr,
                          OriginImpl preferred_origin) throws Exception {
        Instant originTime = preferred_origin.getOriginTime();
        Instant expirationDate = getExpirationDate(originTime);
        return new StringTreeLeaf(this, expirationDate.isAfter(ClockUtil.now()));
    }
    
    public Instant getExpirationDate(Instant originTime) {
        return originTime.plus(expirationAge);
    }

    private Duration expirationAge;
}

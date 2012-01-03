package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;

import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

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
        MicroSecondDate originTime = new MicroSecondDate(preferred_origin.getOriginTime());
        MicroSecondDate expirationDate = getExpirationDate(originTime);
        return new StringTreeLeaf(this, expirationDate.after(ClockUtil.now()));
    }
    
    public MicroSecondDate getExpirationDate(MicroSecondDate originTime) {
        return originTime.add(expirationAge);
    }

    private TimeInterval expirationAge;
}

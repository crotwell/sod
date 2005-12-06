package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
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

    public StringTree accept(EventAccessOperations ev,
                          EventAttr eventAttr,
                          Origin preferred_origin) throws Exception {
        MicroSecondDate originTime = new MicroSecondDate(preferred_origin.origin_time);
        MicroSecondDate expirationDate = originTime.add(expirationAge);
        return new StringTreeLeaf(this, expirationDate.after(ClockUtil.now()));
    }

    private TimeInterval expirationAge;
}

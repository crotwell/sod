package edu.sc.seis.sod.subsetter.availableData;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class PostEventWait implements AvailableDataSubsetter {

    public PostEventWait(Element config) throws ConfigurationException {
        postOriginTime = SodUtil.loadTimeInterval(config);
    }

    public StringTree accept(EventAccessOperations ev,
                          Channel chan,
                          RequestFilter[] orig,
                          RequestFilter[] avail,
                          CookieJar cookies) {
        MicroSecondDate originTime = new MicroSecondDate(EventUtil.extractOrigin(ev).origin_time);
        MicroSecondDate waitTime = originTime.add(postOriginTime);
        if ( ! waitTime.after(ClockUtil.now())) {
        return new StringTreeLeaf(this, false, "Wait until: "+waitTime);
        }
        return new StringTreeLeaf(this, true);
    }

    private TimeInterval postOriginTime;
}
package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

public class PostEventWait implements AvailableDataSubsetter{
    public PostEventWait(Element config) throws ConfigurationException{
        postOriginTime = SodUtil.loadTimeInterval(config);
    }

    public boolean accept(EventAccessOperations ev, Channel chan,
                          RequestFilter[] orig, RequestFilter[] avail,
                          CookieJar cookies){
        MicroSecondDate originTime = new MicroSecondDate(CacheEvent.extractOrigin(ev).origin_time);
        return originTime.add(postOriginTime).after(ClockUtil.now());
    }

    private TimeInterval postOriginTime;
}


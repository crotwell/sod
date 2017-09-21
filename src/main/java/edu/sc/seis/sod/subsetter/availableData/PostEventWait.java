package edu.sc.seis.sod.subsetter.availableData;

import java.time.Duration;
import java.time.Instant;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.util.display.EventUtil;
import edu.sc.seis.sod.util.time.ClockUtil;

public class PostEventWait implements AvailableDataSubsetter {

    public PostEventWait(Element config) throws ConfigurationException {
        postOriginTime = SodUtil.loadTimeInterval(config);
    }

    public StringTree accept(CacheEvent ev,
                             Channel chan,
                          RequestFilter[] request,
                          RequestFilter[] available,
                          MeasurementStorage cookies) {
        Instant originTime = EventUtil.extractOrigin(ev).getOriginTime();
        Instant waitTime = originTime.plus(postOriginTime);
        if ( ! waitTime.isAfter(ClockUtil.now())) {
        return new StringTreeLeaf(this, false, "Wait until: "+waitTime);
        }
        return new StringTreeLeaf(this, true);
    }

    private Duration postOriginTime;
}
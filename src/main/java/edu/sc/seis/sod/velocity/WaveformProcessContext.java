package edu.sc.seis.sod.velocity;

import org.apache.velocity.VelocityContext;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.velocity.network.VelocityChannel;
import edu.sc.seis.sod.velocity.seismogram.VelocityRequest;
import edu.sc.seis.sod.velocity.seismogram.VelocitySeismogram;

/**
 * @author groves Created on May 25, 2005
 */
public class WaveformProcessContext extends VelocityContext {

    public WaveformProcessContext(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  MeasurementStorage cookieJar) {
        ContextWrangler.insertIntoContext(event, this);
        new VelocityChannel((Channel)channel).insertIntoContext(this);
        put("originalRequests", VelocityRequest.wrap(original, channel));
        put("availableRequests", VelocityRequest.wrap(available, channel));
        put("seismograms", VelocitySeismogram.wrap(seismograms, channel));
        put("cookieJar", cookieJar);
    }
}

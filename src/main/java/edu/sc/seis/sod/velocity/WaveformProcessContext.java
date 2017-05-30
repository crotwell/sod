package edu.sc.seis.sod.velocity;

import org.apache.velocity.VelocityContext;

import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.velocity.network.VelocityChannel;
import edu.sc.seis.sod.velocity.network.VelocityInstrumentation;
import edu.sc.seis.sod.velocity.seismogram.VelocityRequest;
import edu.sc.seis.sod.velocity.seismogram.VelocitySeismogram;

/**
 * @author groves Created on May 25, 2005
 */
public class WaveformProcessContext extends VelocityContext {

    public WaveformProcessContext(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) {
        ContextWrangler.insertIntoContext(event, this);
        new VelocityChannel((ChannelImpl)channel).insertIntoContext(this);
        put("instrumentation", new VelocityInstrumentation(Start.getNetworkArm().getNetworkSource(), channel));
        put("originalRequests", VelocityRequest.wrap(original, channel));
        put("availableRequests", VelocityRequest.wrap(available, channel));
        put("seismograms", VelocitySeismogram.wrap(seismograms, channel));
        put("cookieJar", cookieJar);
    }
}

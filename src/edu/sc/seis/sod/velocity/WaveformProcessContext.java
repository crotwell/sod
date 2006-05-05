package edu.sc.seis.sod.velocity;

import org.apache.velocity.VelocityContext;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.velocity.network.VelocityChannel;
import edu.sc.seis.sod.velocity.seismogram.VelocityRequest;
import edu.sc.seis.sod.velocity.seismogram.VelocitySeismogram;

/**
 * @author groves Created on May 25, 2005
 */
public class WaveformProcessContext extends VelocityContext {

    public WaveformProcessContext(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) {
        ContextWrangler.insertIntoContext(event, this);
        new VelocityChannel(channel).insertIntoContext(this);
        put("originalRequests", VelocityRequest.wrap(original, channel));
        put("availableRequests", VelocityRequest.wrap(available, channel));
        put("seismograms", VelocitySeismogram.wrap(seismograms, channel));
        put("cookieJar", cookieJar);
    }
}

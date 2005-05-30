package edu.sc.seis.sod.velocity;

import org.apache.velocity.VelocityContext;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.velocity.event.VelocityEvent;
import edu.sc.seis.sod.velocity.network.VelocityChannel;

/**
 * @author groves
 * 
 * Created on May 30, 2005
 */
public class ContextWrangler {

    public static VelocityContext createContext(EventAccessOperations event) {
        VelocityContext ctx = new VelocityContext();
        insertIntoContext(event, ctx);
        return ctx;
    }

    public static void insertIntoContext(EventAccessOperations event,
                                         VelocityContext ctx) {
        if(event instanceof VelocityEvent) {
            ctx.put("event", event);
        } else if(event instanceof CacheEvent) {
            ctx.put("event", new VelocityEvent((CacheEvent)event));
        } else {
            ctx.put("event", new VelocityEvent(new CacheEvent(event)));
        }
    }

    public static VelocityContext createContext(Channel chan) {
        VelocityContext ctx = new VelocityContext();
        insertIntoContext(chan, ctx);
        return ctx;
    }

    public static void insertIntoContext(Channel chan, VelocityContext ctx) {
        new VelocityChannel(chan).insertIntoContext(ctx);
    }

    public static VelocityContext createContext(EventAccessOperations event,
                                                Channel channel,
                                                RequestFilter[] original,
                                                RequestFilter[] available,
                                                LocalSeismogramImpl[] seismograms,
                                                CookieJar cookieJar) {
        VelocityContext ctx = new WaveformProcessContext(event,
                                                         channel,
                                                         original,
                                                         available,
                                                         seismograms,
                                                         cookieJar);
        return ctx;
    }
}

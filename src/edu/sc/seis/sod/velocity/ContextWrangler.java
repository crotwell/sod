package edu.sc.seis.sod.velocity;

import org.apache.velocity.VelocityContext;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.StationImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.velocity.event.VelocityEvent;
import edu.sc.seis.sod.velocity.network.VelocityChannel;
import edu.sc.seis.sod.velocity.network.VelocityNetwork;
import edu.sc.seis.sod.velocity.network.VelocityStation;
import edu.sc.seis.sod.velocity.seismogram.VelocitySeismogram;

/**
 * @author groves
 * 
 * Created on May 30, 2005
 */
public class ContextWrangler {

    public static VelocityContext createContext() {
        VelocityContext ctx = new VelocityContext();
        ctx.put("formatter", new FissuresFormatter());
        return ctx;
    }

    public static VelocityContext createContext(EventAccessOperations event) {
        VelocityContext ctx = createContext();
        insertIntoContext(event, ctx);
        return ctx;
    }

    public static VelocityContext createContext(StationImpl sta) {
        VelocityContext ctx = createContext();
        VelocityStation velSta = new VelocityStation(sta);
        velSta.insertIntoContext(ctx);
        return ctx;
    }

    public static VelocityEvent insertIntoContext(EventAccessOperations event,
                                                  VelocityContext ctx) {
        VelocityEvent ev = VelocityEvent.wrap(event);
        ctx.put("event", ev);
        return ev;
    }

    public static VelocityContext createContext(NetworkAttr net) {
        VelocityContext ctx = createContext();
        insertIntoContext(net, ctx);
        return ctx;
    }

    public static VelocityNetwork insertIntoContext(NetworkAttr net,
                                                    VelocityContext ctx) {
        VelocityNetwork velNet = VelocityNetwork.wrap(net);
        velNet.insertIntoContext(ctx);
        return velNet;
    }

    public static VelocityContext createContext(Channel chan) {
        VelocityContext ctx = createContext();
        insertIntoContext(chan, ctx);
        return ctx;
    }

    public static VelocityChannel insertIntoContext(Channel chan,
                                                    VelocityContext ctx) {
        VelocityChannel velChan = VelocityChannel.wrap(chan);
        velChan.insertIntoContext(ctx);
        return velChan;
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

    public static VelocitySeismogram insertIntoContext(LocalSeismogramImpl seis,
                                                       Channel chan,
                                                       VelocityContext ctx) {
        VelocitySeismogram velSeis = VelocitySeismogram.wrap(seis,
                                                             insertIntoContext(chan,
                                                                               ctx));
        ctx.put("seismogram", velSeis);
        return velSeis;
    }
}

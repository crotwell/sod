package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.sod.status.FissuresFormatter;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.IteratorTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.tools.generic.RenderTool;

/**
 * CookieJar exists as a way for various subsetters and processors in
 * the waveform arm to pass information down the chain. It is implemented
 * as a Velocity Context which allows the cusomization of output status
 * pages through velocity template files. The Event and Channel are placed in
 * the context with names "sod_event" and "sod_channel".
 *
 *
 * Created: Thu Dec 13 18:18:48 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class CookieJar {

    public CookieJar (EventAccessOperations event, Channel channel){
        context = new VelocityContext(commonContext);
        context.put("sod_event", event);
        context.put("sod_eventAttr", event);
        context.put("sod_channel", channel);

    }

    /**
     * Returns the Velocity Context that stores the put data
     *
     * @return    a  VelocityContext
     */
    public VelocityContext getContext() {
        return context;
    }

    public Object get(String key) {
        return context.get(key);
    }

    public void put(String key, Object value) {
        context.put(key, value);
    }

    VelocityContext context;

    static VelocityContext commonContext = new VelocityContext();

    static {
        commonContext.put("FERegion", ParseRegions.getInstance());
        commonContext.put("fissures", new FissuresFormatter());
        commonContext.put("velocity_date", new DateTool());
        commonContext.put("velocity_math", new MathTool());
        commonContext.put("velocity_number", new NumberTool());
        commonContext.put("velocity_iterator", new IteratorTool());
        commonContext.put("velocity_render", new RenderTool());

    }

}// CookieJar

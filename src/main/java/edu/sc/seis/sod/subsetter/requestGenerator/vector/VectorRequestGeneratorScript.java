package edu.sc.seis.sod.subsetter.requestGenerator.vector;

import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.velocity.event.VelocityEvent;
import edu.sc.seis.sod.velocity.network.VelocityChannelGroup;


public class VectorRequestGeneratorScript extends AbstractScriptSubsetter implements VectorRequestGenerator {

    public VectorRequestGeneratorScript(Element config) {
        super(config);
    }
    
    @Override
    public RequestFilter[][] generateRequest(CacheEvent event, ChannelGroup channelGroup, CookieJar cookieJar)
            throws Exception {
        return runScript(new VelocityEvent(event), new VelocityChannelGroup(channelGroup), cookieJar);
    }

    /** Run the script with the arguments as predefined variables. */
    public RequestFilter[][] runScript(VelocityEvent event,  VelocityChannelGroup channelGroup, CookieJar cookieJar) throws Exception {
        engine.put("event", event);
        engine.put("channelGroup", channelGroup);
        engine.put("cookieJar", cookieJar);
        Object result = preeval();
        if (result == null) {
            // try getting variable named result from engine
            result = engine.get("result");
        }
        if (result == null) {
            throw new Exception("Script had a null result");
        }
        if (result instanceof RequestFilter[][]) {
            return (RequestFilter[][])result;
        } else if (result instanceof List) {
            RequestFilter[][] out = new RequestFilter[((List)result).size()][];
            for (int i = 0; i < out.length; i++) {
                List<RequestFilter> componentList = (List<RequestFilter>)((List)result).get(i);
                out[i] = (RequestFilter[])((List)componentList).toArray(new RequestFilter[0]);
            }
            return out;
        }
        throw new Exception("Script did not return RequestFilter 2D array or List of List: "+result.getClass().getName());
    }
}

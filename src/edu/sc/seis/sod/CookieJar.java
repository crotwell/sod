package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import org.apache.velocity.VelocityContext;

/**
 * CookieJar.java
 *
 *
 * Created: Thu Dec 13 18:18:48 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class CookieJar {

    public CookieJar (EventAccessOperations event, Channel channel){
        context = new VelocityContext();
        context.put("sod.event", event);
        context.put("sod.channel", channel);
    }

    public Object get(String key) {
        return context.get(key);
    }

    public void put(String key, Object value) {
        context.put(key, value);
    }

    VelocityContext context;

}// CookieJar

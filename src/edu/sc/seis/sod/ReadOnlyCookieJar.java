/**
 * ReadOnlyCookieJar.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import org.apache.velocity.VelocityContext;

public class ReadOnlyCookieJar {

    public ReadOnlyCookieJar(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
        this.context = null;
    }

    public ReadOnlyCookieJar(EventAccessOperations event, Channel channel){
        context = new VelocityContext();
        context.put("sod.event", event);
        context.put("sod.channel", channel);
    }

    public Object get(String key) {
        if (cookieJar != null) {
            return cookieJar.get(key);
        } else {
            return context.get(key);
        }
    }

    CookieJar cookieJar;

    VelocityContext context;
}


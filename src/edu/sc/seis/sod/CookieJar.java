package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;

/**
 * CookieJar.java
 *
 *
 * Created: Thu Dec 13 18:18:48 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class CookieJar extends ReadOnlyCookieJar {

    public CookieJar (EventAccessOperations event, Channel channel){
        super(event, channel);
    }

    public void put(String key, Object value) {
        context.put(key, value);
    }

    public ReadOnlyCookieJar getReadOnly() {
        if (readOnly == null) {
            readOnly = new ReadOnlyCookieJar(this);
        }
        return readOnly;
    }

    ReadOnlyCookieJar readOnly;

}// CookieJar

package edu.sc.seis.sod;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.IteratorTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.tools.generic.RenderTool;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.iris.Fissures.network.SiteImpl;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.sod.database.waveform.JDBCVelocityContext;
import edu.sc.seis.sod.status.FissuresFormatter;

/**
 * CookieJar exists as a way for various subsetters and processors in the
 * waveform arm to pass information down the chain. It is implemented as a
 * Velocity Context which allows the cusomization of output status pages through
 * velocity template files. The Event and Channel are placed in the context with
 * names "sod_event" and "sod_channel". Created: Thu Dec 13 18:18:48 2001
 * 
 * @author <a href="mailto:">Philip Crotwell </a>
 * @version
 */
public class CookieJar implements Context {

    Map<String, Serializable> stationCookies;

    Map<String, Serializable> channelCookies = null;

    public CookieJar(CookieEventPair pair,
                     Map<String, Serializable> stationCookies) {
        this.stationCookies = stationCookies;
        this.pair = pair;
    }

    public CookieJar(CookieEventPair pair,
                     Map<String, Serializable> stationCookies,
                     Map<String, Serializable> channelCookies) {
        this(pair, stationCookies);
        this.channelCookies = channelCookies;
    }

    public Object get(String key) {
        if(channelCookies != null && channelCookies.containsKey(key)) {
            return channelCookies.get(key);
        }
        return stationCookies.get(key);
    }

    public void put(String key, Serializable value) {
        if(channelCookies != null) {
            channelCookies.put(key, value);
        } else {
            stationCookies.put(key, value);
        }
    }

    public CookieEventPair getPair() {
        return pair;
    }

    protected CookieEventPair pair;

    public Context getContext() {
        return this;
    }
    
    public boolean containsKey(Object key) {
        if(stationCookies.containsKey(key)) {
            return true;
        }
        if(channelCookies != null && channelCookies.containsKey(key)) {
            return true;
        }
        return false;
    }

    public Object[] getKeys() {
        List out = new ArrayList(stationCookies.keySet());
        if(channelCookies != null) {
            out.addAll(channelCookies.keySet());
        }
        return out.toArray();
    }

    public Object put(String key, Object value) {
        if(value instanceof Serializable) {
            put(key, (Serializable)value);
            return value;
        } else {
            throw new IllegalArgumentException("value must be Serializable: "
                    + value.getClass());
        }
    }

    public Object remove(Object key) {
        throw new RuntimeException("Context is Read Only");
    }
}// CookieJar

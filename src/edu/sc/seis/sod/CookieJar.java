package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelCookieJar;
import edu.sc.seis.sod.database.waveform.JDBCVelocityContext;
import edu.sc.seis.sod.status.FissuresFormatter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
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

    public CookieJar (EventChannelPair ecp) throws SQLException {
        if (jdbcCookie == null) {
            jdbcCookie = new JDBCEventChannelCookieJar();
        }
        memoryContext = getChannelContext(ecp.getEvent(), ecp.getChannel());
        memoryContext.put("sod_cookieJar", this); // needed for eval and recurse velocity tool
        memoryContext.put("status", ecp.getStatus());
        context = new JDBCVelocityContext(ecp, jdbcCookie, memoryContext);
        ((Context)memoryContext.get("sod_site_context")).put(ChannelIdUtil.toString(ecp.getChannel().get_id()), context);
    }

    /**
     * Returns the Velocity Context that stores the put data
     *
     * @return    a  VelocityContext
     */
    public Context getContext() {
        return context;
    }

    public Object get(String key) {
        return context.get(key);
    }

    public void put(String key, Object value) {
        context.put(key, value);
    }

    Context context;

    /** this holds items that are not perisent and can be recreated from the
     * EventChannelPair. */
    Context memoryContext;

    static JDBCEventChannelCookieJar jdbcCookie = null;


    public static Context getChannelContext(EventAccessOperations event,
                                            Channel channel) {
        VelocityContext siteContext = getSiteContext(event, channel.my_site);
        String chanIdStr = ChannelIdUtil.toString(channel.get_id());
        if ( ! siteContext.containsKey(chanIdStr)) {
            VelocityContext chanContext = new VelocityContext(siteContext);
            chanContext.put("channel_code", channel.get_code());
            chanContext.put("channel_id", channel.get_id());
            chanContext.put("sod_channel", channel);
            // I don't like putting this in the channel context,
            // but it is needed to put the database backed context for the
            // channel into the site context for use by other channels in the
            // site
            chanContext.put("sod_site_context", siteContext);
            siteContext.put(chanIdStr, chanContext);
            ((Collection)siteContext.get("allChanIds")).add(chanIdStr);
        }
        return (Context)siteContext.get(chanIdStr);

    }

    public static VelocityContext getSiteContext(EventAccessOperations event,
                                                 Site site) {
        VelocityContext staContext =
            getStationContext(event, site.my_station);
        String siteIdStr = SiteIdUtil.toString(site.get_id());
        if ( ! staContext.containsKey(siteIdStr)) {
            VelocityContext siteContext = new VelocityContext(staContext);
            siteContext.put("site_code", site.get_code());
            siteContext.put("site_id", site.get_id());
            siteContext.put("allChanIds", new LinkedList());
            staContext.put(siteIdStr, siteContext);
            ((Collection)staContext.get("allSiteIds")).add(siteIdStr);
        }
        return (VelocityContext)staContext.get(siteIdStr);
    }

    public static VelocityContext getStationContext(EventAccessOperations event,
                                                    Station sta) {
        VelocityContext nContext = getNetworkContext(event, sta.my_network.get_id());
        String staIdStr = StationIdUtil.toString(sta.get_id());
        if ( ! nContext.containsKey(staIdStr)) {
            VelocityContext staContext = new VelocityContext(nContext);
            staContext.put("station_code", sta.get_code());
            staContext.put("station_id", sta.get_id());
            staContext.put("allSiteIds", new LinkedList());
            nContext.put(staIdStr, staContext);
            ((Collection)nContext.get("allStationIds")).add(staIdStr);
        }
        return (VelocityContext)nContext.get(staIdStr);

    }

    public static VelocityContext getNetworkContext(EventAccessOperations event,
                                                    NetworkId netId) {
        VelocityContext eContext = getEventContext(event);
        String netIdStr = NetworkIdUtil.toString(netId);
        if ( ! eContext.containsKey(netIdStr)) {
            VelocityContext netContext = new VelocityContext(eContext);
            netContext.put("network_code", netId.network_code);
            netContext.put("network_id", netId);
            netContext.put("allStationIds", new LinkedList());
            eContext.put(netIdStr, netContext);
        }
        return (VelocityContext)eContext.get(netIdStr);

    }

    public synchronized static VelocityContext getEventContext(EventAccessOperations event) {
        if ( ! eventContexts.containsKey(event)) {
            // check size and kill oldest if too big
            if (eventOrder.size() >= MAX_EVENTS) {
                EventAccessOperations oldEvent = (EventAccessOperations)eventOrder.getLast();
                cleanEvent(oldEvent);
            }
            VelocityContext eContext = new VelocityContext(commonContext);
            eContext.put("sod_event", event);
            eventContexts.put(event, eContext);
            eventOrder.addFirst(event);
        }
        return (VelocityContext)eventContexts.get(event);
    }

    /** removes storage for VelocityContexts associated with the given
     * event. Should be called once there is no longer any chance of
     * further processing for an event. */
    public static void cleanEvent(EventAccessOperations event) {
        logger.debug("Cleaning event "+event);
        eventOrder.remove(event);
        eventContexts.remove(event);
    }

    private static int MAX_EVENTS = 10;

    private static LinkedList eventOrder = new LinkedList();

    private static HashMap eventContexts = new HashMap();

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

    private static final Logger logger = Logger.getLogger(CookieJar.class);


}// CookieJar


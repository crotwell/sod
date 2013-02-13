package edu.sc.seis.sod.source.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAccessSeqHolder;
import edu.iris.Fissures.IfEvent.EventSeqIter;
import edu.iris.Fissures.IfEvent.EventSeqIterHolder;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.GlobalAreaImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.cache.ProxyEventDC;
import edu.sc.seis.fissuresUtil.cache.RetryEventAccessOperations;
import edu.sc.seis.fissuresUtil.cache.VestingEventDC;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.subsetter.DepthRange;
import edu.sc.seis.sod.subsetter.origin.Catalog;
import edu.sc.seis.sod.subsetter.origin.Contributor;
import edu.sc.seis.sod.subsetter.origin.MagnitudeRange;
import edu.sc.seis.sod.subsetter.origin.OriginDepthRange;

public class EventDCQuerier {

    public EventDCQuerier(String serverName, String serverDNS, int numRetries, Element config)
            throws ConfigurationException {
        this.serverName = serverName;
        this.serverDNS = serverDNS;
        this.numRetries = numRetries;
        NodeList childNodes = config.getChildNodes();
        List configCatalogs = new ArrayList();
        List configContributors = new ArrayList();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                if(!tagName.equals("name") && !tagName.equals("dns")
                        && !tagName.equals("retries")
                        && !tagName.equals("originTimeRange")) {
                    Object object = SodUtil.load((Element)node,
                                                 new String[] {"eventArm",
                                                               "origin"});
                    if(tagName.equals("originDepthRange")) {
                        DepthRange dr = ((OriginDepthRange)object);
                        minDepth = dr.getMinDepth();
                        maxDepth = dr.getMaxDepth();
                    } else if(tagName.equals("magnitudeRange")) {
                        magRange = (MagnitudeRange)object;
                    } else if(object instanceof edu.iris.Fissures.Area) {
                        area = (edu.iris.Fissures.Area)object;
                    } else if(tagName.equals("catalog")) {
                        configCatalogs.add(((Catalog)object).getCatalog());
                    } else if(tagName.equals("contributor")) {
                        configContributors.add(((Contributor)object).getContributor());
                    }
                }
            }
        }
        catalogs = (String[])configCatalogs.toArray(new String[configCatalogs.size()]);
        contributors = (String[])configContributors.toArray(new String[configContributors.size()]);
    }

    public ProxyEventDC getEventDC() {
        if(eventDC == null) {
            eventDC = new VestingEventDC(serverName,
                                         serverDNS,
                                         CommonAccess.getNameService(),
                                         Start.createRetryStrategy(getRetries()));
        }
        return eventDC;
    }

    public CacheEvent[] query(MicroSecondTimeRange tr) {
        logger.debug("querying for events from " + tr);
        EventAccessOperations[] events;
        EventSeqIterHolder holder = new EventSeqIterHolder();
        events = getEvents(tr, holder);
        logger.debug("got " + events.length + " events from query " + tr);
        if(holder.value != null) {
            // might be events in the iterator...
            LinkedList allEvents = new LinkedList();
            for(int j = 0; j < events.length; j++) {
                allEvents.add(events[j]);
            }
            EventSeqIter iterator = holder.value;
            EventAccessSeqHolder eHolder = new EventAccessSeqHolder();
            while(iterator.how_many_remain() > 0) {
                iterator.next_n(sequenceMaximum, eHolder);
                EventAccess[] iterEvents = eHolder.value;
                for(int j = 0; j < iterEvents.length; j++) {
                    allEvents.add(iterEvents[j]);
                }
            }
            events = (EventAccessOperations[])allEvents.toArray(new EventAccessOperations[0]);
        }
        CacheEvent[] cached = cacheEvents(events);
        Arrays.sort(cached, new Comparator() {

            public int compare(Object first, Object second) {
                MicroSecondDate firstOrigin = new MicroSecondDate(((CacheEvent)first).getOrigin().getOriginTime());
                MicroSecondDate secondOrigin = new MicroSecondDate(((CacheEvent)second).getOrigin().getOriginTime());
                return firstOrigin.compareTo(secondOrigin);
            }
        });
        return cached;
    }

    private EventAccessOperations[] getEvents(MicroSecondTimeRange tr,
                                              EventSeqIterHolder holder) {
        return getEventDC().a_finder()
                .query_events(area,
                              minDepth,
                              maxDepth,
                              tr.getFissuresTimeRange(),
                              magRange.getSearchTypes(),
                              (float)magRange.getMinValue(),
                              (float)magRange.getMaxValue(),
                              catalogs,
                              contributors,
                              sequenceMaximum,
                              holder);
    }

    private CacheEvent[] cacheEvents(EventAccessOperations[] uncached) {
        CacheEvent[] cached = new CacheEvent[uncached.length];
        for(int counter = 0; counter < cached.length; counter++) {
            if(uncached[counter] instanceof CacheEvent) {
                cached[counter] = (CacheEvent)uncached[counter];
            } else {
                cached[counter] = new CacheEvent(new RetryEventAccessOperations(uncached[counter], 3));
            }
            cached[counter].get_attributes();
            // Call get_origins to cache it, and reorder the magnitudes so
            // that one that passed is first while we're at it
            Origin[] origins = cached[counter].get_origins();
            for(int i = 0; i < origins.length; i++) {
                putPassingMagFirst(origins[i], magRange);
            }
            putPassingMagFirst(EventUtil.extractOrigin(cached[counter]),
                               magRange);
        }
        return cached;
    }

    /**
     * Reorder the magnitudes in o such that a magnitude >= minMag and <= maxMag
     * with a type in searchTypes is in the 0th position of the magnitudes array
     * 
     * @return - True if a magnitude is found and moved into the 0th position
     */
    public static boolean putPassingMagFirst(Origin o, MagnitudeRange magRange) {
        Magnitude[] acceptable = magRange.getAcceptable(o.getMagnitudes());
        if(acceptable.length > 0) {
            Magnitude zeroth = o.getMagnitudes()[0];
            for(int i = 0; i < o.getMagnitudes().length; i++) {
                if(o.getMagnitudes()[i].equals(acceptable[0])) {
                    o.getMagnitudes()[0] = acceptable[0];
                    o.getMagnitudes()[i] = zeroth;
                    return true;
                }
            }
        }
        return false;
    }

    public String toString() {
        return "EventDCQuerier(" + serverDNS + "/" + serverName + ")";
    }

    public int getRetries() {return numRetries;}
    
    
    public String[] getCatalogs() {
        return catalogs;
    }

    
    public String[] getContributors() {
        return contributors;
    }

    
    public Quantity getMinDepth() {
        return minDepth;
    }

    
    public Quantity getMaxDepth() {
        return maxDepth;
    }

    
    public String getServerName() {
        return serverName;
    }

    
    public String getServerDNS() {
        return serverDNS;
    }

    
    public MagnitudeRange getMagRange() {
        return magRange;
    }

    
    public edu.iris.Fissures.Area getArea() {
        return area;
    }

    private int sequenceMaximum = 100;

    private ProxyEventDC eventDC;

    private String[] catalogs, contributors;

    private Quantity minDepth = new QuantityImpl(-90000.0, UnitImpl.KILOMETER);

    private Quantity maxDepth = new QuantityImpl(90000.0, UnitImpl.KILOMETER);

    private String serverName, serverDNS;

    private MagnitudeRange magRange = new MagnitudeRange();

    private edu.iris.Fissures.Area area = new GlobalAreaImpl();
    
    private int numRetries;

    private static Logger logger = LoggerFactory.getLogger(EventDCQuerier.class);
}

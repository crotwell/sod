package edu.sc.seis.sod.source.event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAccessSeqHolder;
import edu.iris.Fissures.IfEvent.EventSeqIter;
import edu.iris.Fissures.IfEvent.EventSeqIterHolder;
import edu.iris.Fissures.model.GlobalAreaImpl;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.BulletproofVestFactory;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventLoader;
import edu.sc.seis.fissuresUtil.cache.ProxyEventDC;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.DepthRange;
import edu.sc.seis.sod.subsetter.origin.Catalog;
import edu.sc.seis.sod.subsetter.origin.Contributor;
import edu.sc.seis.sod.subsetter.origin.MagnitudeRange;
import edu.sc.seis.sod.subsetter.origin.OriginDepthRange;

public class EventDCQuerier {

    public EventDCQuerier(String serverName, String serverDNS, Element config)
            throws ConfigurationException {
        this.serverName = serverName;
        this.serverDNS = serverDNS;
        NodeList childNodes = config.getChildNodes();
        List configCatalogs = new ArrayList();
        List configContributors = new ArrayList();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                if(!tagName.equals("name") && !tagName.equals("dns")) {
                    Object object = SodUtil.load((Element)node,
                                                 new String[] {"eventArm",
                                                               "origin"});
                    if(tagName.equals("originDepthRange")) {
                        DepthRange dr = ((OriginDepthRange)object);
                        minDepth = dr.getMinDepth();
                        maxDepth = dr.getMaxDepth();
                    } else if(tagName.equals("magnitudeRange")) {
                        MagnitudeRange magRange = (MagnitudeRange)object;
                        minMag = (float)magRange.getMinValue();
                        maxMag = (float)magRange.getMaxValue();
                        searchTypes = magRange.getSearchTypes();
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
            CommonAccess common = CommonAccess.getCommonAccess();
            FissuresNamingService fissName = common.getFissuresNamingService();
            eventDC = BulletproofVestFactory.vestEventDC(serverDNS,
                                                         serverName,
                                                         fissName);
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
        return cacheEvents(events);
    }

    private EventAccessOperations[] getEvents(MicroSecondTimeRange tr,
                                              EventSeqIterHolder holder) {
        return getEventDC().a_finder().query_events(area,
                                                    minDepth,
                                                    maxDepth,
                                                    tr.getFissuresTimeRange(),
                                                    searchTypes,
                                                    minMag,
                                                    maxMag,
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
                cached[counter] = new CacheEvent(uncached[counter]);
                // preload cache
                EventLoader backLoader = new EventLoader(cached[counter]);
                WorkerThreadPool.getDefaultPool().invokeLater(backLoader);
            }
        }
        return cached;
    }

    private int sequenceMaximum = 100;

    private ProxyEventDC eventDC;

    private String[] catalogs, contributors;

    private Quantity minDepth = new QuantityImpl(-90000.0, UnitImpl.KILOMETER);

    private Quantity maxDepth = new QuantityImpl(90000.0, UnitImpl.KILOMETER);

    private String[] searchTypes = {"%"};

    private String serverName, serverDNS;

    private float minMag = -99.0f, maxMag = 99.0f;

    private edu.iris.Fissures.Area area = new GlobalAreaImpl();

    private static Logger logger = Logger.getLogger(EventDCQuerier.class);
}

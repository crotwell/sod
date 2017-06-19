package edu.sc.seis.sod.source.event;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.NotFound;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.MicroSecondTimeRange;
import edu.sc.seis.sod.model.event.CacheEvent;

        
public class BackwardsEventSource extends AbstractEventSource {


    protected BackwardsEventSource(EventSource source) {
        super("backwards "+source.getName(), source.getRetries());
        this.wrappedSource = source;
    }

    public BackwardsEventSource(Element config) throws ConfigurationException {
        super(config, "BackwardsEventSource");
        NodeList children = config.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if(node instanceof Element) {
                Element el = (Element)node;
                if (el.getLocalName().equals("name")) {
                    this.name = SodUtil.getNestedText(el);
                } else {
                    Object o = SodUtil.load(el, "event"); // loads something from source.event package
                    if (o instanceof EventSource) {
                        wrappedSource = (EventSource)o;
                        break;
                    }
                }
            }
        }
    }

    public MicroSecondTimeRange getEventTimeRange() {
        return wrappedSource.getEventTimeRange();
    }

    public boolean hasNext() {
        MicroSecondTimeRange currentQuery = getQueryTime();
        MicroSecondDate queryBegin = getEventTimeRange().getBeginTime();
        logger.debug("Checking if more queries to the event server are in order. "
                + "The last query was  "
                + currentQuery
                + " and we're querying from " + currentQuery.getBeginTime().subtract(increment));
        return ! currentQuery.getEndTime().equals(queryBegin);
    }

    public CacheEvent[] next() {
        CacheEvent[] results = wrappedSource.next();
        CacheEvent[] out = new CacheEvent[results.length];
        for(int i = 0; i < out.length; i++) {
            out[i] = results[results.length-i-1];
        }
        return out;
    }
    
    protected boolean caughtUpWithRealtime() {
        // going backwards so never catch up
        return false;
    }
    
    protected void updateQueryEdge(MicroSecondTimeRange queryTime) {
        setQueryEdge(queryTime.getBeginTime());
    }
    
    /**
     * @return - the next time to start asking for events
     */
    protected MicroSecondDate getQueryStart() {
        try {
            return getQueryEdge();
        } catch(NotFound e) {
            logger.debug("the query times database didn't have an entry for our server/dns combo, just use the time in the config file");
            return getEventTimeRange().getEndTime();
        }
    }

    /**
     * @return - the next time range to be queried for events
     */
    protected MicroSecondTimeRange getQueryTime() {
        MicroSecondDate queryEnd = getQueryStart();
        MicroSecondDate queryStart = queryEnd.subtract(increment);
        if(getEventTimeRange().getBeginTime().after(queryStart)) {
            queryStart = getEventTimeRange().getBeginTime();
        }
        return new MicroSecondTimeRange(queryStart, queryEnd);
    }
    

    EventSource wrappedSource;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BackwardsEventSource.class);
}

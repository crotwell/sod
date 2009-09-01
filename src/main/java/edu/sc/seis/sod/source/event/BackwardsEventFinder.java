package edu.sc.seis.sod.source.event;

import org.w3c.dom.Element;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;


public class BackwardsEventFinder extends EventFinder {

    public BackwardsEventFinder(Element config) throws Exception {
        super(config);
    }
    
    public String getDescription() {
        return "Backwards "+super.getDescription();
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
        CacheEvent[] results = super.next();
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
        } catch(edu.sc.seis.fissuresUtil.database.NotFound e) {
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
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(BackwardsEventFinder.class);
}

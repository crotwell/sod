package edu.sc.seis.sod.hibernate.eventpair;

import java.time.Instant;

import edu.sc.seis.sod.util.time.ClockUtil;



public abstract class WaveformWorkUnit implements Runnable {
    
    protected WaveformWorkUnit() {
        numRetries=0;
        lastQuery = ClockUtil.now();
    }
    
    protected long dbid;
    protected int numRetries;
    protected Instant lastQuery;
    
    public static final org.slf4j.Logger failLogger = org.slf4j.LoggerFactory.getLogger("Fail.WaveformArm");

    public void updateRetries() {
        // use setters for hibernate auto-dirty checking
    	setNumRetries(getNumRetries()+1);
        setLastQuery(ClockUtil.now());
    }
    
    public long getDbid() {
        return dbid;
    }

    
    protected void setDbid(long dbid) {
        this.dbid = dbid;
    }

    
    public int getNumRetries() {
        return numRetries;
    }

    
    protected void setNumRetries(int numRetries) {
        this.numRetries = numRetries;
    }

    
    public Instant getLastQuery() {
        return lastQuery;
    }

    
    protected void setLastQuery(Instant lastQuery) {
        this.lastQuery = lastQuery;
    }
    
    public boolean equals(Object o) {
        if (! (o instanceof WaveformWorkUnit)) {
            return false;
        }
        WaveformWorkUnit w = (WaveformWorkUnit)o;
        return w.dbid == dbid && w.numRetries == numRetries && w.lastQuery.equals(lastQuery);
    }
    
    public int hashCode() {
        return 89+17*(int)dbid+41*numRetries+17*lastQuery.hashCode();
    }
}

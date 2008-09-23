package edu.sc.seis.sod;

import java.sql.Timestamp;

import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.sod.hibernate.SodDB;


public abstract class WaveformWorkUnit implements Runnable {
    
    protected WaveformWorkUnit() {
        numRetries=0;
        lastQuery = ClockUtil.now().getTimestamp();
    }
    
    protected long dbid;
    protected int numRetries;
    protected Timestamp lastQuery;
    
    protected static SodDB sodDb = new SodDB();

    public static final org.apache.log4j.Logger failLogger = org.apache.log4j.Logger.getLogger("Fail.WaveformArm");

    public void updateRetries() {
        // use setters for hibernate auto-dirty checking
    	setNumRetries(getNumRetries()+1);
        setLastQuery(ClockUtil.now().getTimestamp());
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

    
    public Timestamp getLastQuery() {
        return lastQuery;
    }

    
    protected void setLastQuery(Timestamp lastQuery) {
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

package edu.sc.seis.sod;

import java.time.Duration;
import java.time.Instant;

import edu.sc.seis.sod.util.time.ClockUtil;


public class QueryTime {
    
    public QueryTime() {}


    public QueryTime(String serverName, Instant time) {
        this.serverName = serverName;
        this.time = time;
    }
    
    protected int dbid;
    protected String serverName;
    protected Instant time;
    
    public boolean needsRefresh(Duration refreshInterval) {
        Instant lastTime = getTime();
        Instant currentTime = ClockUtil.now();
        Duration timeInterval = Duration.between(lastTime, currentTime).abs();
        if(timeInterval.compareTo(refreshInterval) > 0) {
            return true;
        }
        return false;
    }
    
    public int getDbid() {
        return dbid;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public Instant getTime() {
        return time;
    }

    
    protected void setDbid(int dbid) {
        this.dbid = dbid;
    }

    
    protected void setServerName(String serverName) {
        this.serverName = serverName;
    }

    
    public void setTime(Instant time) {
        this.time = time;
    }
    
    public boolean equals(Object o) {
        if (o instanceof QueryTime) {
            QueryTime q = (QueryTime)o;
            return getDbid() == q.getDbid() && getServerName().equals(q.getServerName()) && getTime().equals(q.getTime());
        } else {
            return false;
        }
    }
    
    public int hashCode() {
        return 89+17*getDbid()+getServerName().hashCode()+19*getTime().hashCode();   
    }

    public long delayMillisUntilNextRefresh(Duration refreshInterval) {
        Instant now = ClockUtil.now();
        Instant nextRefresh = getTime().plus(refreshInterval);
        if (nextRefresh.isBefore(now)) {
            return 0l;
        }
        return Duration.between(now, nextRefresh).toMillis();  
    }
}

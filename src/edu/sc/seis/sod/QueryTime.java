package edu.sc.seis.sod;

import java.sql.Timestamp;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;


public class QueryTime {
    
    public QueryTime() {}

    public QueryTime(String serverName, String serverDNS, Timestamp time) {
        this.serverName = serverName;
        this.serverDNS = serverDNS;
        this.time = time;
    }
    
    protected int dbid;
    protected String serverName;
    protected String serverDNS;
    protected Timestamp time;
    
    public boolean needsRefresh(TimeInterval refreshInterval) {
        MicroSecondDate lastTime = new MicroSecondDate(getTime());
        MicroSecondDate currentTime = ClockUtil.now();
        TimeInterval timeInterval = currentTime.difference(lastTime);
        timeInterval = (TimeInterval)timeInterval.convertTo(refreshInterval.getUnit());
        if(timeInterval.getValue() >= refreshInterval.getValue()) {
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
    
    public String getServerDNS() {
        return serverDNS;
    }
    
    public Timestamp getTime() {
        return time;
    }

    
    protected void setDbid(int dbid) {
        this.dbid = dbid;
    }

    
    protected void setServerName(String serverName) {
        this.serverName = serverName;
    }

    
    protected void setServerDNS(String serverDNS) {
        this.serverDNS = serverDNS;
    }

    
    public void setTime(Timestamp time) {
        this.time = time;
    }
    
    public boolean equals(Object o) {
        if (o instanceof QueryTime) {
            QueryTime q = (QueryTime)o;
            return getDbid() == q.getDbid() && getServerDNS().equals(q.getServerDNS()) && getServerName().equals(q.getServerName()) && getTime().equals(q.getTime());
        } else {
            return false;
        }
    }
    
    public int hashCode() {
        return 89+17*getDbid()+17*getServerDNS().hashCode()+getServerName().hashCode()+17*getTime().hashCode();   
    }
}

package edu.sc.seis.sod.hibernate;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;

public class RecordSectionItem {

    public RecordSectionItem(String orientationId,
                             String recordSectionId,
                             CacheEvent event,
                             Channel channel,
                             float sToN,
                             boolean inBest) {
        super();
        this.event = event;
        this.channel = channel;
        this.recordSectionId = recordSectionId;
        this.orientationId = orientationId;
        this.inBest = inBest;
        this.sToN = sToN;
        DistAz distAz = new DistAz(channel, event);
        this.degrees = (float)distAz.getDelta();
    }

    /** for hibernate */
    protected RecordSectionItem() {}

    protected long dbid;

    protected CacheEvent event;

    protected Channel channel;

    protected String recordSectionId;

    protected String orientationId;

    protected boolean inBest;
    
    protected float sToN;
    
    protected float degrees;

    public long getDbid() {
        return dbid;
    }

    protected void setDbid(long dbid) {
        this.dbid = dbid;
    }

    public CacheEvent getEvent() {
        return event;
    }

    protected void setEvent(CacheEvent event) {
        this.event = event;
    }

    public Channel getChannel() {
        return channel;
    }

    protected void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getRecordSectionId() {
        return recordSectionId;
    }

    protected void setRecordSectionId(String recordSectionId) {
        this.recordSectionId = recordSectionId;
    }

    public String getOrientationId() {
        return orientationId;
    }

    protected void setOrientationId(String orientationId) {
        this.orientationId = orientationId;
    }

    public boolean isInBest() {
        return inBest;
    }

    public void setInBest(boolean inBest) {
        this.inBest = inBest;
    }
    
    public float getsToN() {
        return sToN;
    }
    
    protected void setsToN(float sToN) {
        this.sToN = sToN;
    }
    
    /** event to station distance in degrees. */
    public float getDegrees() {
        return degrees;
    }
    
    public void setDegrees(float degrees) {
        this.degrees = degrees;
    }
}

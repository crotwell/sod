package edu.sc.seis.sod.hibernate;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;

public class RecordSectionItem {

    public RecordSectionItem(String orientationId,
                             String recordSectionId,
                             CacheEvent event,
                             Channel channel,
                             boolean inBest) {
        super();
        this.event = event;
        this.channel = channel;
        this.recordSectionId = recordSectionId;
        this.orientationId = orientationId;
        this.inBest = inBest;
    }

    /** for hibernate */
    protected RecordSectionItem() {}

    long dbid;

    CacheEvent event;

    Channel channel;

    String recordSectionId;

    String orientationId;

    boolean inBest;

    public long getDbid() {
        return dbid;
    }

    protected void setDbid(long dbid) {
        this.dbid = dbid;
    }

    public CacheEvent getEvent() {
        return event;
    }

    public void setEvent(CacheEvent event) {
        this.event = event;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getRecordSectionId() {
        return recordSectionId;
    }

    public void setRecordSectionId(String recordSectionId) {
        this.recordSectionId = recordSectionId;
    }

    public String getOrientationId() {
        return orientationId;
    }

    public void setOrientationId(String orientationId) {
        this.orientationId = orientationId;
    }

    public boolean isInBest() {
        return inBest;
    }

    public void setInBest(boolean inBest) {
        this.inBest = inBest;
    }
}

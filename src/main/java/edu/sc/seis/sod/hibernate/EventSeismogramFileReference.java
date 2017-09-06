package edu.sc.seis.sod.hibernate;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.SeismogramAttrImpl;


public class EventSeismogramFileReference extends AbstractSeismogramFileReference {

    /** just for hibernate */
    protected EventSeismogramFileReference() {}
    
    public EventSeismogramFileReference(CacheEvent event,
                                        Channel channel,
                                        SeismogramAttrImpl seis,
                                        String fileLocation,
                                        SeismogramFileTypes filetype) {
        super(channel.getNetworkCode(), 
              channel.getStationCode(),
              channel.getLocCode(),
              channel.getChannelCode(),
              seis.getBeginTime(),
              seis.getEndTime(),
              fileLocation,
              filetype.getIntValue());
        this.event = event;
    }

    protected CacheEvent event;

    
    public CacheEvent getEvent() {
        return event;
    }

    
    protected void setEvent(CacheEvent event) {
        this.event = event;
    }
    
    
}

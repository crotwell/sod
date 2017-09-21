package edu.sc.seis.sod.subsetter.eventChannel;

import org.json.JSONObject;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * EventChannelSubsetter.java Created: Thu Dec 13 17:19:47 2001
 * 
 * @author Philip Crotwell 
 */
public interface EventChannelSubsetter extends Subsetter {

    public StringTree accept(CacheEvent event,
                          Channel channel,
                          MeasurementStorage channelMeasurements) throws Exception;
}// EventChannelSubsetter

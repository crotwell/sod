package edu.sc.seis.sod.subsetter.availableData;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * AvailableDataSubsetter.java Created: Thu Dec 13 17:18:32 2001
 * 
 * @author Philip Crotwell
 */
public interface AvailableDataSubsetter extends Subsetter {

    public StringTree accept(CacheEvent event,
                             Channel channel,
                             RequestFilter[] request,
                             RequestFilter[] available,
                             MeasurementStorage cookieJar) throws Exception;
}// AvailableDataSubsetter

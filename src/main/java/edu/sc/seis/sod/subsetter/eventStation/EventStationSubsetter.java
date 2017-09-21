package edu.sc.seis.sod.subsetter.eventStation;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * EventStationSubsetter.java Created: Thu Dec 13 17:18:32 2001
 *
 * @author Philip Crotwell
 */
public interface EventStationSubsetter extends Subsetter {

    public StringTree accept(CacheEvent event,
                          Station station,
                          MeasurementStorage cookieJar) throws Exception;
}// EventStationSubsetter

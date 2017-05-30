package edu.sc.seis.sod.subsetter.eventStation;

import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * EventStationSubsetter.java Created: Thu Dec 13 17:18:32 2001
 *
 * @author Philip Crotwell
 */
public interface EventStationSubsetter extends Subsetter {

    public StringTree accept(CacheEvent event,
                          StationImpl station,
                          CookieJar cookieJar) throws Exception;
}// EventStationSubsetter

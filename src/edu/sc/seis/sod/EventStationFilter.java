package edu.sc.seis.sod;

/**
 * EventStationFilter.java
 *
 *
 * Created: Thu Dec 13 17:18:32 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface EventStationFilter {

    public boolean accept(Event event, Station station, CookieJar cookies);
    
}// EventStationFilter

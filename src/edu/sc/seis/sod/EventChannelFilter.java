package edu.sc.seis.sod;

/**
 * EventChannelFilter.java
 *
 *
 * Created: Thu Dec 13 17:19:47 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface EventChannelFilter {

    public boolean accept(Event event, Channel channel, CookieJar cookies);
    
}// EventChannelFilter

package edu.sc.seis.sod;

/**
 * ChannelIdFilter.java
 *
 *
 * Created: Thu Dec 13 17:15:04 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface ChannelIdFilter {

    public boolean accept(ChannelId channelId, CookieJar cookies);
    
}// ChannelIdFilter

package edu.sc.seis.sod;

/**
 * ChannelFilter.java
 *
 *
 * Created: Thu Dec 13 17:15:47 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface ChannelFilter {

    public boolean accept(Channel channel, CookieJar cookies);
    
}// ChannelFilter

package edu.sc.seis.sod;

/**
 * NetworkIdFilter.java
 *
 *
 * Created: Thu Dec 13 17:09:18 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface NetworkIdFilter {

    public boolean accept(NetworkId networkId, CookieJar cookies);
    
}// NetworkIdFilter

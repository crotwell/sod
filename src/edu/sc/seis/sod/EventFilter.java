package edu.sc.seis.sod;

/**
 * EventFilter.java
 *
 *
 * Created: Thu Dec 13 17:03:44 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface EventFilter {

    public boolean accept(EventAccessOperations event, CookieJar cookies);

    
}// EventFilter

package edu.sc.seis.sod;

/**
 * LocalSeismogramFilter.java
 *
 *
 * Created: Thu Dec 13 18:01:05 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface LocalSeismogramFilter {
    
    public boolean accept(Event event, 
			  Channel channel, 
			  LocalSeismogram[] seismograms,
			  CookieJar cookies);

    
}// LocalSeismogramFilter

package edu.sc.seis.sod;

/**
 * LocalSeismogramProcessor.java
 *
 *
 * Created: Thu Dec 13 18:03:03 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface LocalSeismogramProcessor {

    public void process(Event event, 
			Channel channel, 
			LocalSeismogram[] seismograms, 
			CookieJar cookies);
    
}// LocalSeismogramProcessor

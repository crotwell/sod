package edu.sc.seis.sod;

/**
 * MotionVectorFilter.java
 *
 *
 * Created: Thu Dec 13 17:59:58 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class MotionVectorFilter {

    public boolean accept(Event event, 
			  Channel channel, 
			  LocalMotionVector[] motionVectors,
			  CookieJar cookies);

    
}// MotionVectorFilter

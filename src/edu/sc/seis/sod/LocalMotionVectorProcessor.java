package edu.sc.seis.sod;

/**
 * LocalMotionVectorProcessor.java
 *
 *
 * Created: Thu Dec 13 18:11:22 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface LocalMotionVectorProcessor {


    public void process(Event event, 
			Channel channel, 
			MotionVector[] vectors, 
			CookieJar cookies);
    
    
}// LocalMotionVectorProcessor

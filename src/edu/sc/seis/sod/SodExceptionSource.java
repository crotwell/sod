package edu.sc.seis.sod;

import java.util.*;
/**
 * SodExceptionSource.java
 *
 *
 * Created: Fri Apr 26 11:02:06 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class SodExceptionSource {
    public SodExceptionSource (){
	
    }

    public void addSodExceptionListener(SodExceptionListener sodExceptionListener) {

	vector.add(sodExceptionListener);
    }

    public void removeSodExceptionListener(SodExceptionListener sodExceptionListener) {

	vector.remove(sodExceptionListener);
	
    }

    public void notifyListeners(java.lang.Object object, Exception e) {

	for(int counter = 0; counter < vector.size(); counter++) {
	    SodExceptionListener sodExceptionListener = (SodExceptionListener)vector.get(counter);
	    sodExceptionListener.sodExceptionHandler(new SodException(object, e));
	}
    }
    
    private Vector vector = new Vector();
    
}// SodExceptionSource

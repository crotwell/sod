package edu.sc.seis.sod;

import java.util.*;

import org.apache.log4j.*;

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

    /**
     * adds a sodExceptionListener.
     *
     */


    public void addSodExceptionListener(SodExceptionListener sodExceptionListener) {

    vector.add(sodExceptionListener);
    }

    /**
     * removes the sodExceptionListener.
     */

    public void removeSodExceptionListener(SodExceptionListener sodExceptionListener) {

    vector.remove(sodExceptionListener);

    }

    /**
     * notifys the Listeners about the occurence of a sodException.
     */

    public void notifyListeners(java.lang.Object object, Throwable e) {

    for(int counter = 0; counter < vector.size(); counter++) {
        SodExceptionListener sodExceptionListener = (SodExceptionListener)vector.get(counter);
        sodExceptionListener.sodExceptionHandler(new SodException(object, e));
    }
    }

    //vector to hold the sodExceptionListeners.
    private Vector vector = new Vector();

    static Category logger =
    Category.getInstance(SodExceptionSource.class.getName());

}// SodExceptionSource

package edu.sc.seis.sod;
import edu.sc.seis.fissuresUtil.exceptionHandler.WrappedException;

/**
 * InvalidDatabaseStateException.java
 *
 *
 * Created: Mon Jan 27 11:39:50 2003
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla </a>$Id: InvalidDatabaseStateException.java 6172 2003-10-22 15:55:01Z groves $
 * @version
 */

public class InvalidDatabaseStateException extends Exception implements WrappedException {
    public InvalidDatabaseStateException (){

    }

    public InvalidDatabaseStateException(String s) {
    super(s);
    }

    public InvalidDatabaseStateException(String s, Throwable e) {

    super(s, e);
    }

    public Throwable getCausalException() {
    return getCause();
    }


}// InvalidDatabaseStateException

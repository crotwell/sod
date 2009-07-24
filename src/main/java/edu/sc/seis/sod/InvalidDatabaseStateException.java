package edu.sc.seis.sod;

/**
 * InvalidDatabaseStateException.java
 *
 *
 * Created: Mon Jan 27 11:39:50 2003
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla </a>$Id: InvalidDatabaseStateException.java 7937 2004-03-31 18:35:44Z crotwell $
 * @version
 */

public class InvalidDatabaseStateException extends Exception {
    public InvalidDatabaseStateException (){

    }

    public InvalidDatabaseStateException(String s) {
    super(s);
    }

    public InvalidDatabaseStateException(String s, Throwable e) {
    super(s, e);
    }

}// InvalidDatabaseStateException

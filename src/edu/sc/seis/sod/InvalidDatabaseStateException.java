package edu.sc.seis.sod;
import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.WrappedException;

/**
 * InvalidDatabaseStateException.java
 *
 *
 * Created: Mon Jan 27 11:39:50 2003
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla </a>$Id: InvalidDatabaseStateException.java 3201 2003-01-27 17:16:33Z telukutl $
 * @version
 */

public class InvalidDatabaseStateException extends Exception implements WrappedException {
    public InvalidDatabaseStateException (){
	
    }

    public InvalidDatabaseStateException(String s) {
	super(s);
    }

    public InvalidDatabaseStateException(String s, Exception e) {

	super(s);
	causalException = e;
    }

    public Exception getCausalException() {
	return this.causalException;
    }
    
    protected Exception causalException = null;
    
   
}// InvalidDatabaseStateException

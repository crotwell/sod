package edu.sc.seis.sod.util.exceptionHandler;

/**
 * @author oliverpa Created on Sep 9, 2004
 */
public class WindowConnectionInterceptor implements ExceptionInterceptor {

    public boolean handle(String message, Throwable t) {
        if(t instanceof InternalError
                && t.getMessage().matches("^Can't connect .* window server .*")) {
            System.out.println("You're trying to run this program without a window server connection.");
	    System.exit(0);
        }
        return false;
    }
    
}

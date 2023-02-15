package edu.sc.seis.sod.util.exceptionHandler;


/**
 * @author oliverpa
 * Created on Sep 9, 2004
 */
public interface ExceptionInterceptor {

    /*
     * The message is passed just in case there may be
     * some useful information included within...
     */
    public boolean handle(String message, Throwable t);
    
}

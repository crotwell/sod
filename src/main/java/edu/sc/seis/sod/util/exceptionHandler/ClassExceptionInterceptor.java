package edu.sc.seis.sod.util.exceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClassExceptionInterceptor implements ExceptionInterceptor {

    public ClassExceptionInterceptor(Class<? extends Throwable> exceptionClass) {
        logger.info("GlobalExceptionHandler ignoring "+exceptionClass.getName());
        this.exceptionClass = exceptionClass;
    }
    
    public boolean handle(String message, Throwable t) {
        if(exceptionClass.isInstance(t) || (exceptionClass.isInstance(t.getCause()))) {
            logger.info(exceptionClass.getName()+", ignoring.", t);
            return true;
        }
        return false;
    }
    
    Class<? extends Throwable> exceptionClass;

    private static Logger logger = LoggerFactory.getLogger(ClassExceptionInterceptor.class);
}

package edu.sc.seis.sod;

import java.util.*;

/**
 * SodExceptionListener.java
 *
 *
 * Created: Fri Apr 26 10:51:29 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public interface  SodExceptionListener extends EventListener{
    public void sodExceptionHandler(SodException sodException);
}// SodExceptionListener

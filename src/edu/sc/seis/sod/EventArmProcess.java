package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.*;

/**
 * EventProcess.java
 *
 *
 * Created: Tue Mar 19 14:10:08 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public interface EventArmProcess extends Process {

    /**
     * Describe <code>process</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param cookieJar a <code>CookieJar</code> value
     * @exception Exception if an error occurs
     */
    public void process(EventAccessOperations event, CookieJar cookieJar) throws Exception;
    
}// NetworkProcess

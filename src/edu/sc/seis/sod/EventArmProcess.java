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

    public void process(EventAccessOperations event, CookieJar cookieJar);
    
}// NetworkProcess

package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.*;

/**
 * NetworkProcess.java
 *
 *
 * Created: Tue Mar 19 14:10:08 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public interface EventArmProcess extends Process {

    public void process(EventAccess network, Origin origin, CookieJar cookieJar);
    
}// NetworkProcess

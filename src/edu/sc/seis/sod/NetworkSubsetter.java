package edu.sc.seis.sod;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * NetworkSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:13:46 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface NetworkSubsetter {

    public boolean accept(NetworkAccessOperations network, CookieJar cookies);
    
}// NetworkSubsetter

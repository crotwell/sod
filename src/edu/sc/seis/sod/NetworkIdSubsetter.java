package edu.sc.seis.sod;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * NetworkIdSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:09:18 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface NetworkIdSubsetter extends Subsetter{

    public boolean accept(NetworkId networkId, CookieJar cookies);
    
}// NetworkIdSubsetter

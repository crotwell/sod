package edu.sc.seis.sod;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * NetworkAttrSubsetter.java
 *
 * Created: Thu Dec 13 17:03:44 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface NetworkAttrSubsetter extends Subsetter {

    public boolean accept(NetworkAttr event, CookieJar cookies) throws Exception;

    
}// NetworkSubsetter

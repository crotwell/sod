package edu.sc.seis.sod.subsetter.networkArm;
import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;
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

public interface NetworkSubsetter extends Subsetter {

    /**
     * Describe <code>accept</code> method here.
     *
     * @param event a <code>NetworkAttr</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(NetworkAttr attr, CookieJar cookies) throws Exception;


}// NetworkSubsetter

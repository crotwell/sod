package edu.sc.seis.sod.subsetter.network;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * NetworkAttrSubsetter.java
 *
 * Created: Thu Dec 13 17:03:44 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface NetworkSubsetter extends Subsetter {

    public boolean accept(NetworkAttr attr) throws Exception;


}// NetworkSubsetter

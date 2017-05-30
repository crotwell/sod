package edu.sc.seis.sod.subsetter.network;
import edu.sc.seis.sod.model.station.NetworkAttrImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * NetworkAttrSubsetter.java
 *
 * Created: Thu Dec 13 17:03:44 2001
 *
 * @author Philip Crotwell
 */

public interface NetworkSubsetter extends Subsetter {

    public StringTree accept(NetworkAttrImpl network) throws Exception;


}// NetworkSubsetter

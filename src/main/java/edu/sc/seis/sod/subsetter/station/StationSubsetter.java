package edu.sc.seis.sod.subsetter.station;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * StationSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:05:33 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface StationSubsetter extends Subsetter{

    public StringTree accept(StationImpl station, NetworkAccess network) throws Exception;

}// StationSubsetter

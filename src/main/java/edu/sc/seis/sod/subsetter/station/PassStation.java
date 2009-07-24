package edu.sc.seis.sod.subsetter.station;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

/**
 * PassStation.java
 *
 *
 * Created: Thu Dec 13 17:09:18 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class  PassStation implements StationSubsetter{

    public StringTree accept(Station station, NetworkAccess network) { return new Pass(this);  }

}// PassStation

package edu.sc.seis.sod.subsetter.station;

import edu.iris.Fissures.IfNetwork.Station;

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

    public boolean accept(Station station) { return true;  }

}// PassStation

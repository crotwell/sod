package edu.sc.seis.sod.subsetter.networkArm;
import edu.iris.Fissures.IfNetwork.Station;
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

    public boolean accept(Station stations) throws Exception;

}// StationSubsetter

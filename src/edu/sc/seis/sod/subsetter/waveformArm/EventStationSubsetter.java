package edu.sc.seis.sod.subsetter.waveformArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * EventStationSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:18:32 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface EventStationSubsetter extends Subsetter{

    public boolean accept(EventAccessOperations event,  Station station)
        throws Exception;

}// EventStationSubsetter

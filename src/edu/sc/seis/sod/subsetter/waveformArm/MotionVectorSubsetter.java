package edu.sc.seis.sod.subsetter.waveformArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.LocalMotionVector;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * MotionVectorFilter.java
 *
 *
 * Created: Thu Dec 13 17:59:58 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface MotionVectorSubsetter extends Subsetter {
    public boolean accept(EventAccessOperations event, Channel[] channels,
              LocalMotionVector motionVectors) throws Exception;

}// MotionVectorFilter

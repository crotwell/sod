package edu.sc.seis.sod.process.waveformArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.LocalMotionVector;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;

/**
 * LocalMotionVectorProcessor.java
 *
 *
 * Created: Thu Dec 13 18:11:22 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface LocalMotionVectorProcess extends WaveformArmProcess {

    public void process(EventAccessOperations event, NetworkAccess network,
            Channel[] channels, RequestFilter[] original,
            RequestFilter[] available,LocalMotionVector vector) throws Exception;


}// LocalMotionVectorProcessor

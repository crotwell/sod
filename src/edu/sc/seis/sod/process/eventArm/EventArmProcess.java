package edu.sc.seis.sod.process.eventArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.process.Process;

/**
 * EventProcess.java
 *
 *
 * Created: Tue Mar 19 14:10:08 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public interface EventArmProcess extends Process {

    public void process(EventAccessOperations event) throws Exception;

}// NetworkProcess

package edu.sc.seis.sod.process.networkArm;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.sod.process.Process;

/**
 * NetworkProcess.java
 *
 *
 * Created: Tue Mar 19 14:10:08 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public interface NetworkArmProcess extends Process {

    public void process(NetworkAccess network,Channel chan) throws Exception;

}// NetworkProcess

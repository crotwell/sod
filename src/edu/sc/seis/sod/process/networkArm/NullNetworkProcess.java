package edu.sc.seis.sod.process.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;

/**
 * NullNetworkProcess.java
 * Created: Tue Mar 19 14:08:39 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public class NullNetworkProcess implements NetworkArmProcess {
    public NullNetworkProcess (){}

    public void process(NetworkAccess network, Channel channel) {}
}// NullNetworkProcess

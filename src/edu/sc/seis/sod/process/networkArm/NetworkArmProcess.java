package edu.sc.seis.sod.process.networkArm;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.sod.CookieJar;
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

    /**
     * Describe <code>process</code> method here.
     *
     * @param network a <code>NetworkAccess</code> value
     * @param chan a <code>Channel</code> value
     * @param cookieJar a <code>CookieJar</code> value
     * @exception Exception if an error occurs
     */
    public void process(NetworkAccess network,
            Channel chan,
            CookieJar cookieJar) throws Exception;
    
}// NetworkProcess

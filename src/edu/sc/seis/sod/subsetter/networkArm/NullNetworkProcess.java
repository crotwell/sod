package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import org.w3c.dom.*;

import java.io.*;

/**
 * NullNetworkProcess.java
 * Created: Tue Mar 19 14:08:39 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public class NullNetworkProcess implements NetworkArmProcess {
    /**
     * Creates a new <code>NullNetworkProcess</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public NullNetworkProcess (Element config){
	    
    }

    /**
     * Describe <code>process</code> method here.
     *
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     */
    public void process(NetworkAccess network, Channel channel, CookieJar cookies) {
    }
   
}// NullNetworkProcess

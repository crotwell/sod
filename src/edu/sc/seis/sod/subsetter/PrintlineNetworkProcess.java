package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfNetwork.*;
import org.w3c.dom.*;

/**
 * PrintlineNetworkProcess.java
 *
 *
 * Created: Tue Mar 19 14:08:39 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public class PrintlineNetworkProcess implements NetworkProcess {
    public PrintlineNetworkProcess (Element config){
	
    }

    public void process(NetworkAccess network) {
	System.out.println(network.get_attributes().name);
    }
}// PrintlineNetworkProcess

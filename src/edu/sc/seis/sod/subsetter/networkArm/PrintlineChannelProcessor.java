package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfNetwork.*;
import org.w3c.dom.*;

/**
 * PrintlineChannelProcessor.java
 *
 *
 * Created: Tue Mar 19 14:08:39 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public class PrintlineChannelProcessor implements NetworkProcess {
    public PrintlineChannelProcessor (Element config){

	process(null);
    }

    public void process(NetworkAccess network) {
	//System.out.println(network.get_attributes().name);
	System.out.println("The Channel Process must be executed");
    }
}// PrintlineChannelProcessor

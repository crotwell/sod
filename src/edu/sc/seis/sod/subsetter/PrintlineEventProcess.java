package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfEvent.*;
import org.w3c.dom.*;

/**
 * PrintlineEventProcess.java
 *
 *
 * Created: Tue Mar 19 14:08:39 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public class PrintlineEventProcess implements EventProcess {
    public PrintlineEventProcess (Element config){
	
    }

    public void process(EventAccessOperations event) {
	System.out.println(event.get_attributes().name);
    }
}// PrintlineEventProcess

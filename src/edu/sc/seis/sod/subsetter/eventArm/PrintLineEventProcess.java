package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfEvent.*;
import org.w3c.dom.*;

import java.io.*;

/**
 * PrintLineEventProcess.java
 *
 *
 * Created: Tue Mar 19 14:08:39 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public class PrintLineEventProcess implements EventArmProcess {
    public PrintLineEventProcess (Element config){
	
    }

    public void process(EventAccess event, Origin origin, CookieJar cookies) {
	try {
	    FileWriter fwriter = new FileWriter("_my_event_temp_", true);
	    BufferedWriter bwriter = new BufferedWriter(fwriter);
	    bwriter.write(event.get_attributes().name, 0, event.get_attributes().name.length());
	    bwriter.newLine();
	    bwriter.close();
	} catch(Exception e) {
	    
	    System.out.println("Exception caught while writing to file in PrintLineChannelProcess");
	}
    }
}// PrintLineEventProcess

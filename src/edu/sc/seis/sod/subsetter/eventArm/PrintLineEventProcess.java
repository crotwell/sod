package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfEvent.*;
import org.w3c.dom.*;

import java.io.*;

/**
 * &lt;printLineEventProcess/&gt;
 */


public class PrintLineEventProcess implements EventArmProcess {
    /**
     * Creates a new <code>PrintLineEventProcess</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public PrintLineEventProcess (Element config){
	
    }

    /**
     * Describe <code>process</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param cookies a <code>CookieJar</code> value
     */
    public void process(EventAccessOperations event, CookieJar cookies) {
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

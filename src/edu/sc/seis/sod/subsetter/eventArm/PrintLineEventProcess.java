package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfEvent.*;
import org.w3c.dom.*;

import java.io.*;

/**
 *<pre>
 * &lt;printLineEventProcess/&gt;
 *</pre>
 */


public class PrintLineEventProcess implements EventArmProcess {
    /**
     * Creates a new <code>PrintLineEventProcess</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public PrintLineEventProcess (Element config){
        filename = SodUtil.getNestedText(config);
    }

    /**
     * Describe <code>process</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param cookies a <code>CookieJar</code> value
     */
    public void process(EventAccessOperations event, CookieJar cookies) {
	String eventStr = 
	    regions.getRegionName(event.get_attributes().region);
	try {
	    eventStr =event.get_preferred_origin().magnitudes[0].type+" "+
            event.get_preferred_origin().magnitudes[0].value+" "+eventStr;
	    eventStr+=" "+event.get_preferred_origin().origin_time.date_time;
	} catch (NoPreferredOrigin e) {
	    eventStr+=" No Pref Origin!";
	} // end of try-catch
	

	if (filename != null) {
	    try {
		FileWriter fwriter = new FileWriter("_my_event_temp_", true);
		BufferedWriter bwriter = new BufferedWriter(fwriter);
		bwriter.write(eventStr, 0, eventStr.length());
		bwriter.newLine();
		bwriter.close();
	    } catch(Exception e) {
	    
		//ntln("Exception caught while writing to file in PrintLineChannelProcess");
	    }
	} else {
             System.out.println(eventStr);
	} // end of else
	
    }

    protected String filename = null;

    protected static edu.sc.seis.fissuresUtil.display.ParseRegions regions 
	= new edu.sc.seis.fissuresUtil.display.ParseRegions();;

}// PrintLineEventProcess

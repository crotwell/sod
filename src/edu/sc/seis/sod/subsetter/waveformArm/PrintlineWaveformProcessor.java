package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import org.w3c.dom.*;

import java.io.*;

/**
 * PrintlineWaveformProcessor.java
 *
 *
 * Created: Tue Mar 19 14:08:39 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public class PrintlineWaveformProcessor implements WaveFormArmProcess {
    public PrintlineWaveformProcessor (Element config){
	    
    }

    public void process(CookieJar cookies) {
	try {
	   
	} catch(Exception e) {
	    
	    System.out.println("Exception caught while writing to file in PrintLineWaveformProcess");
	}
	
    }
   
}// PrintlineWaveformProcessor

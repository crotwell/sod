package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.database.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import org.w3c.dom.*;
import java.io.*;


/**
 * WaveformStatusProcess.java
 *
 *
 * Created: Fri Oct 18 14:57:48 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class NullWaveformStatusProcess implements WaveformStatusProcess {
    public NullWaveformStatusProcess (Element config){
    }

    public NullWaveformStatusProcess (){
    }

    public void begin(EventAccessOperations eventAccess) {
    }

    public void begin(EventAccessOperations eventAccess, 
		      NetworkAccess networkAccess) {
    }

    public void begin(EventAccessOperations eventAccess, 
		      Station station) {
    }
    
    public void begin(EventAccessOperations eventAccess, 
		      Site site) {
    }
    
    public void begin(EventAccessOperations eventAccess, 
		      Channel channel) {
    }

    public void end(EventAccessOperations eventAccess, 
		    Channel channel, 
		    Status status, 
		    String reason) {
    }
    
    public void end(EventAccessOperations eventAccess, 
		    Site site) {
    }
    
    public void end(EventAccessOperations eventAccess, 
		    Station station) {
    }

    public void end(EventAccessOperations eventAccess, 
		    NetworkAccess networkAccess) {
    }

    public void end(EventAccessOperations eventAccess) {
    }

    public void closeProcessing() {
    }

}// WaveformStatusProcess

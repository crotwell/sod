package edu.sc.seis.sod;

import edu.sc.seis.sod.subsetter.*;
import edu.sc.seis.sod.subsetter.waveFormArm.*;

import edu.sc.seis.fissuresUtil.cache.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import edu.iris.Fissures.IfSeismogramDC.*;

import org.w3c.dom.*;
import org.apache.log4j.*;

/**
 * WaveFormArmThread.java
 *
 *
 * Created: Mon Apr 15 09:22:06 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class WaveFormArmThread extends SodExceptionSource implements Runnable{
    public WaveFormArmThread (EventAccess eventAccess, 
			      EventStationSubsetter eventStationSubsetter,
			      FixedDataCenter fixedDataCenterSubsetter, 
			      LocalSeismogramArm localSeismogramArm,
			      Channel[] successfulChannels, WaveFormArm parent, 
			      SodExceptionListener sodExceptionListener){
	this.eventAccess = eventAccess;
	this.eventStationSubsetter = eventStationSubsetter;
	this.fixedDataCenterSubsetter = fixedDataCenterSubsetter;
	this.localSeismogramArm = localSeismogramArm;
	this.networkArm = networkArm;
	this.successfulChannels = successfulChannels;
	this.parent = parent;
	addSodExceptionListener(sodExceptionListener);
    }

    public void run() {
	try {
	    processWaveFormArm(eventAccess);
	} catch(Exception ce) {
	    ce.printStackTrace();
	    notifyListeners(this, ce);
	}
	
    }

     /**
     * Describe <code>processWaveFormArm</code> method here.
     *
     * @param eventAccess an <code>EventAccess</code> value
     * @exception Exception if an error occurs
     */
    public void processWaveFormArm(EventAccess eventAccess) throws Exception{
	for(int counter = 0; counter < successfulChannels.length; counter++) {
	     if(eventStationSubsetter.accept(eventAccess, null, successfulChannels[counter].my_site.my_station, null)) {
		 DataCenter dataCenter = fixedDataCenterSubsetter.getSeismogramDC();
		 localSeismogramArm.processLocalSeismogramArm(eventAccess, null, successfulChannels[counter], dataCenter);
		    }
	  	}
	parent.signalWaveFormArm();
    }

     private EventAccess eventAccess;
    
    private EventStationSubsetter eventStationSubsetter = null;//new NullEventStationSubsetter();

    private FixedDataCenter fixedDataCenterSubsetter = null;

    private LocalSeismogramArm localSeismogramArm = null;

    private NetworkArm networkArm;

    private Channel[] successfulChannels;
    
    private WaveFormArm parent;

    static Category logger = 
	Category.getInstance(WaveFormArmThread.class.getName());
    
}// WaveFormArmThread

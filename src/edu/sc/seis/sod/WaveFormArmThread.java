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

public class WaveFormArmThread implements Runnable{
    public WaveFormArmThread (EventAccess eventAccess, 
			      EventStationSubsetter eventStationSubsetter,
			      LocalSeismogramArm localSeismogramArm,
			      Channel[] successfulChannels){
	this.eventAccess = eventAccess;
	this.eventStationSubsetter = eventStationSubsetter;
	this.localSeismogramArm = localSeismogramArm;
	this.networkArm = networkArm;
	this.successfulChannels = successfulChannels;
    }

    public void run() {
	try {
	    if(eventAccess == null){ System.out.println("EventAccess is NULL");System.exit(0);}
	    processWaveFormArm(eventAccess);
	} catch(Exception ce) {
	    ce.printStackTrace();
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
	    System.out.println("Calling accept on eventStationSubsetter");
	    if(eventStationSubsetter == null) System.out.println("NULL");
	    else System.out.println("NOT NULL");
	    if(eventStationSubsetter.accept(eventAccess, null, successfulChannels[counter].my_site.my_station, null)) {
		localSeismogramArm.processLocalSeismogramArm(eventAccess, null, successfulChannels[counter]);  
	    }
	}
	
    }

    private EventAccess eventAccess;
    
    private EventStationSubsetter eventStationSubsetter = null;//new NullEventStationSubsetter();

    private LocalSeismogramArm localSeismogramArm = null;

    private NetworkArm networkArm;

    private Channel[] successfulChannels;

    static Category logger = 
	Category.getInstance(WaveFormArmThread.class.getName());
    
}// WaveFormArmThread

package edu.sc.seis.sod;

import edu.sc.seis.sod.subsetter.*;
import edu.sc.seis.sod.database.*;
import edu.sc.seis.sod.subsetter.waveFormArm.*;

import edu.sc.seis.fissuresUtil.cache.*;
import edu.sc.seis.sod.database.*;

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
    public WaveFormArmThread (EventAccessOperations eventAccess, 
			      EventStationSubsetter eventStationSubsetter,
			      SeismogramDCLocator seismogramDCLocator, 
			      LocalSeismogramArm localSeismogramArm,
			      NetworkAccess networkAccess,
			      Channel[] successfulChannels, WaveFormArm parent, 
			      SodExceptionListener sodExceptionListener){
	this.eventAccess = eventAccess;
	this.networkAccess = networkAccess;
	this.eventStationSubsetter = eventStationSubsetter;
	this.seismogramDCLocator = seismogramDCLocator;
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
     * @param eventAccess an <code>EventAccessOperations</code> value
     * @exception Exception if an error occurs
     */
    public void processWaveFormArm(EventAccessOperations eventAccess) throws Exception{
	for(int counter = 0; counter < successfulChannels.length; counter++) {
	    boolean bESS;
		    synchronized(eventStationSubsetter) {
		bESS = eventStationSubsetter.accept(eventAccess, 
						 networkAccess, 
						 successfulChannels[counter].my_site.my_station, 
						 null);
		if(!bESS) {
			parent.setFinalStatus(eventAccess,
					      successfulChannels[counter],
					      Status.COMPLETE_REJECT,
					      "EventStationSubsetterFailed");
		}
	    }
	    if( bESS ) {
		DataCenter dataCenter;
		synchronized(seismogramDCLocator) {
		    dataCenter = seismogramDCLocator.getSeismogramDC(eventAccess, 
								     networkAccess,
								     successfulChannels[counter].my_site.my_station,
								     null);
		}
		localSeismogramArm.processLocalSeismogramArm(eventAccess, 
							     networkAccess, 
							     successfulChannels[counter], 
							     dataCenter,
							     parent);
/*	Start.getQueue().setFinalStatus((EventAccess)((CacheEvent)eventAccess).getEventAccess(), 
				     Status.COMPLETE_SUCCESS);*/
	}//end of if
	}//end of for
	//parent.signalWaveFormArm();
    }

     private EventAccessOperations eventAccess;

    private NetworkAccess networkAccess;
    
    private EventStationSubsetter eventStationSubsetter = null;//new NullEventStationSubsetter();

    private SeismogramDCLocator seismogramDCLocator = null;

    private LocalSeismogramArm localSeismogramArm = null;

    private NetworkArm networkArm;

    private Channel[] successfulChannels;
    
    private WaveFormArm parent;

  
    static Category logger = 
	Category.getInstance(WaveFormArmThread.class.getName());
    
}// WaveFormArmThread

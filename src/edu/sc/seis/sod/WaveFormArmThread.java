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
    public WaveFormArmThread (EventDbObject eventAccess, 
			      EventStationSubsetter eventStationSubsetter,
			      SeismogramDCLocator seismogramDCLocator, 
			      LocalSeismogramArm localSeismogramArm,
			      NetworkDbObject networkAccess,
			      ChannelDbObject[] successfulChannels, 
			      WaveFormArm parent, 
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
	} catch(Throwable ce) {
	    ce.printStackTrace();
//	    notifyListeners(this, ce);
	}
	
    }

     /**
     * Describe <code>processWaveFormArm</code> method here.
     *
     * @param eventAccess an <code>EventAccessOperations</code> value
     * @exception Exception if an error occurs
     */
    public void processWaveFormArm(EventDbObject eventDbObject) throws Throwable{

	EventAccessOperations eventAccess = eventDbObject.getEventAccess();
	if (successfulChannels[0]  == null) System.out.println("Chan is NULL");
	else System.out.println("channel is NOT NULL");


	for(int counter = 0; counter < successfulChannels.length; counter++) {
	    if(eventDbObject.getDbId() == 11 && successfulChannels[counter].getDbId() == 27) {
		System.out.println("got the needed one IN PROCESS WAVEFORM ARM");
		System.out.println("The eventid is "+eventDbObject.getDbId());
		System.out.println("The channelid is "+successfulChannels[counter].getDbId());
		//	System.exit(0);
	}
	parent.setFinalStatus(eventDbObject,
			      successfulChannels[counter],
			      Status.PROCESSING,
			      "WaveformArmProcessingStarted");
	    boolean bESS;
		    synchronized(eventStationSubsetter) {
		bESS = eventStationSubsetter.accept(eventAccess, 
						 networkAccess.getNetworkAccess(), 
						 successfulChannels[counter].getChannel().my_site.my_station, 
						 null);
		if(!bESS) {
		    parent.setFinalStatus(eventDbObject,
					  successfulChannels[counter],
					  Status.COMPLETE_REJECT,
					  "EventStationSubsetterFailed");
		}
	    }
	    if( bESS ) {
		DataCenter dataCenter;
		parent.setFinalStatus(eventDbObject,
				      successfulChannels[counter],
				      Status.PROCESSING,
				      "EventStationSubsetterSucceeded");
		synchronized(seismogramDCLocator) {
		    dataCenter = seismogramDCLocator.getSeismogramDC(eventAccess, 
								     networkAccess.getNetworkAccess(),
								     successfulChannels[counter].getChannel().my_site.my_station,
								     null);
		}
		localSeismogramArm.processLocalSeismogramArm(eventDbObject, 
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

     private EventDbObject eventAccess;

    private NetworkDbObject networkAccess;
    
    private EventStationSubsetter eventStationSubsetter = null;//new NullEventStationSubsetter();

    private SeismogramDCLocator seismogramDCLocator = null;

    private LocalSeismogramArm localSeismogramArm = null;

    private NetworkArm networkArm;

    private ChannelDbObject[] successfulChannels;
    
    private WaveFormArm parent;

  
    static Category logger = 
	Category.getInstance(WaveFormArmThread.class.getName());
    
}// WaveFormArmThread

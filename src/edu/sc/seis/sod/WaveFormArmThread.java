package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.database.ChannelDbObject;
import edu.sc.seis.sod.database.EventDbObject;
import edu.sc.seis.sod.database.NetworkDbObject;
import edu.sc.seis.sod.database.Status;
import edu.sc.seis.sod.subsetter.waveFormArm.LocalSeismogramArm;
import org.apache.log4j.Category;

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
                              LocalSeismogramArm localSeismogramArm,
                              NetworkDbObject networkAccess,
                              ChannelDbObject[] successfulChannels,
                              WaveFormArm parent,
                              SodExceptionListener sodExceptionListener){
        this.eventAccess = eventAccess;
        this.networkAccess = networkAccess;
        this.eventStationSubsetter = eventStationSubsetter;
        this.localSeismogramArm = localSeismogramArm;
        this.successfulChannels = successfulChannels;
        this.parent = parent;
        addSodExceptionListener(sodExceptionListener);
    }

    public void run() {
        try {

            processWaveFormArm(eventAccess);
        } catch(Throwable ce) {
            logger.error("Waveform processing thread dies unexpectantly. ",
                         ce);
            //      notifyListeners(this, ce);
        }

    }

    /**
     * starts the processing of the WaveformArm.
     *
     * @param eventAccess an <code>EventAccessOperations</code> value
     * @exception Exception if an error occurs
     */
    public void processWaveFormArm(EventDbObject eventDbObject) throws Throwable{

        EventAccessOperations eventAccess = eventDbObject.getEventAccess();
        if (successfulChannels[0]  == null) logger.debug("Chan is NULL");
        else logger.debug("channel is NOT NULL");
        for(int counter = 0; counter < successfulChannels.length; counter++) {
            if(eventDbObject.getDbId() == 11 && successfulChannels[counter].getDbId() == 27) {
                logger.debug("got the needed one IN PROCESS WAVEFORM ARM");
                logger.debug("The eventid is "+eventDbObject.getDbId());
                logger.debug("The channelid is "+successfulChannels[counter].getDbId());
                //  System.exit(0);
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
                parent.setFinalStatus(eventDbObject,
                                      successfulChannels[counter],
                                      Status.PROCESSING,
                                      "EventStationSubsetterSucceeded");

                localSeismogramArm.processLocalSeismogramArm(eventDbObject,
                                                             networkAccess,
                                                             successfulChannels[counter],
                                                             parent);
                /*  Start.getQueue().setFinalStatus((EventAccess)((CacheEvent)eventAccess).getEventAccess(),
                 Status.COMPLETE_SUCCESS);*/
            }//end of if
        }//end of for
        //parent.signalWaveFormArm();
    }

    private EventDbObject eventAccess;

    private NetworkDbObject networkAccess;

    private EventStationSubsetter eventStationSubsetter = null;//new NullEventStationSubsetter();


    private LocalSeismogramArm localSeismogramArm = null;

    private NetworkArm networkArm;

    private ChannelDbObject[] successfulChannels;

    private WaveFormArm parent;


    static Category logger =
        Category.getInstance(WaveFormArmThread.class.getName());

}// WaveFormArmThread

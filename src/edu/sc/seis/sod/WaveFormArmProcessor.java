package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.database.ChannelDbObject;
import edu.sc.seis.sod.database.EventDbObject;
import edu.sc.seis.sod.database.NetworkDbObject;
import edu.sc.seis.sod.database.Status;
import edu.sc.seis.sod.database.waveform.EventChannelCondition;
import edu.sc.seis.sod.subsetter.waveFormArm.LocalSeismogramArm;
import org.apache.log4j.Logger;

public class WaveFormArmProcessor implements Runnable{
    public WaveFormArmProcessor (EventDbObject eventAccess,
                                 EventStationSubsetter eventStationSubsetter,
                                 LocalSeismogramArm localSeismogramArm,
                                 NetworkDbObject networkAccess,
                                 ChannelDbObject successfulChannel,
                                 WaveFormArm parent, int pairId){
        this.eventDbObject = eventAccess;
        this.networkAccess = networkAccess;
        this.eventStationSubsetter = eventStationSubsetter;
        this.localSeismogramArm = localSeismogramArm;
        this.successfulChannel = successfulChannel;
        this.parent = parent;
        this.pairId = pairId;
    }
    
    public void run() {
        try {
            EventAccessOperations eventAccess = eventDbObject.getGetEvent();
            EventChannelPair cur = new EventChannelPair(networkAccess,
                                                        eventDbObject,
                                                        successfulChannel,
                                                        parent, pairId);
            cur.update("Processing Started", EventChannelCondition.SUBSETTER_STARTED);
            boolean passedESS;
            synchronized(eventStationSubsetter) {
                try {
                    passedESS = eventStationSubsetter.accept(eventAccess,
                                                             networkAccess.getNetworkAccess(),
                                                             successfulChannel.getChannel().my_site.my_station,
                                                             null);
                    if(!passedESS) {
                        cur.update("Event Station Subsetter Failed",
                                   EventChannelCondition.SUBSETTER_FAILED);
                    }else{
                        cur.update("Event Station Subsetter Succeeded",
                                   EventChannelCondition.SUBSETTER_PASSED);
                        try {
                            localSeismogramArm.processLocalSeismogramArm(cur);
                        } catch (Exception e) {
                            cur.update(e,
                                       "System failure in the local seismogram arm processor",
                                       EventChannelCondition.FAILURE);
                        }
                    }//end of if
                } catch (Exception e) {
                    cur.update(e,
                               "System failure in event station subsetter",
                               EventChannelCondition.FAILURE);
                }
            }//end of for
        } catch(Throwable ce) {
            CommonAccess.handleException(ce,
                                         "Waveform processing thread dies unexpectantly.");
        }
        
    }
    
    private EventDbObject eventDbObject;
    
    private NetworkDbObject networkAccess;
    
    private EventStationSubsetter eventStationSubsetter;
    
    private LocalSeismogramArm localSeismogramArm;
    
    private NetworkArm networkArm;
    
    private ChannelDbObject successfulChannel;
    
    private WaveFormArm parent;
    
    private int pairId;
    
    private static Logger logger = Logger.getLogger(WaveFormArmProcessor.class);
}// WaveFormArmThread

/**
 * EventChannelPairMocker.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.waveFormArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.database.ChannelDbObject;
import edu.sc.seis.sod.database.EventDbObject;
import edu.sc.seis.sod.subsetter.MockFissures;



public class MockECP{
    public static EventChannelPair getECP(){
        return getECP(MockFissures.createEvent());
    }
    
    public static EventChannelPair getECP(Channel chan){
        return getECP(MockFissures.createEvent(), chan);
    }
    
    public static EventChannelPair getECP(EventAccessOperations event) {
        return getECP(event, MockFissures.createChannel());
    }
    
    public static EventChannelPair getECP(EventAccessOperations ev, Channel chan){
        return new EventChannelPair(null, new EventDbObject(0, ev),
                                    new ChannelDbObject(0, chan), null);
    }
    
}

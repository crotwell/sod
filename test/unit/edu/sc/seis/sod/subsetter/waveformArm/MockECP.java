/**
 * EventChannelPairMocker.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.waveFormArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.database.ChannelDbObject;
import edu.sc.seis.sod.database.EventDbObject;



public class MockECP{
    public static EventChannelPair getECP(){
        return getECP(MockEventAccessOperations.createEvent());
    }
    
    public static EventChannelPair getECP(Channel chan){
        return getECP(MockEventAccessOperations.createEvent(), chan);
    }
    
    public static EventChannelPair getECP(EventAccessOperations event) {
        return getECP(event, MockChannel.createChannel());
    }
    
    public static EventChannelPair getECP(EventAccessOperations ev, Channel chan){
        return new EventChannelPair(null, new EventDbObject(0, ev),
                                    new ChannelDbObject(0, chan), null);
    }
    
}

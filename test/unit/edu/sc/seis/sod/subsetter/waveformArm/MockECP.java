/**
 * EventChannelPairMocker.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.waveformArm;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.database.ChannelDbObject;
import edu.sc.seis.sod.database.EventDbObject;
import java.sql.SQLException;



public class MockECP{
    public static EventChannelPair getECP(){
        return getECP(MockEventAccessOperations.createEvent());
    }

    public static EventChannelPair getECP(Channel chan){
        return getECP(MockEventAccessOperations.createEvent(), chan);
    }

    public static EventChannelPair getECP(CacheEvent event) {
        return getECP(event, MockChannel.createChannel());
    }

    public static EventChannelPair getECP(CacheEvent ev, Channel chan){
        try {
        return new EventChannelPair( new EventDbObject(0, ev),
                                    new ChannelDbObject(0, chan), null, 0);
        } catch (SQLException e) {
            GlobalExceptionHandler.handle("Cannot create EventChannelPair", e);
            return null;
        }
    }

}

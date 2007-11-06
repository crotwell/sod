/**
 * EventChannelPairMocker.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.mock;
import java.sql.SQLException;

import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.sod.EventChannelPair;



public class MockECP{
    public static EventChannelPair getECP(){
        return getECP(MockEventAccessOperations.createEvent());
    }

    public static EventChannelPair getECP(ChannelImpl chan){
        return getECP(MockEventAccessOperations.createEvent(), chan);
    }

    public static EventChannelPair getECP(CacheEvent event) {
        return getECP(event, MockChannel.createChannel());
    }

    public static EventChannelPair getECP(CacheEvent ev, ChannelImpl chan){
        try {
        return new EventChannelPair( ev,
                                    chan, 0);
        } catch (SQLException e) {
            GlobalExceptionHandler.handle("Cannot create EventChannelPair", e);
            return null;
        }
    }

}

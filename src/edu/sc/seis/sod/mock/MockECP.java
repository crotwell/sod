/**
 * EventChannelPairMocker.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.mock;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.hibernate.StatefulEvent;



public class MockECP{
    public static EventChannelPair getECP(){
        return getECP(MockStatefulEvent.create());
    }

    public static EventChannelPair getECP(ChannelImpl chan){
        return getECP(MockStatefulEvent.create(), chan);
    }

    public static EventChannelPair getECP(StatefulEvent event) {
        return getECP(event, MockChannel.createChannel());
    }

    public static EventChannelPair getECP(StatefulEvent ev, ChannelImpl chan){
        return new EventChannelPair( ev,
                                    chan, 0, Status.get(Stage.PROCESSOR, Standing.IN_PROG));
    }

}

/**
 * EventChannelPairMocker.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.mock;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.hibernate.eventpair.EventChannelPair;
import edu.sc.seis.sod.hibernate.eventpair.EventStationPair;
import edu.sc.seis.sod.mock.station.MockChannel;
import edu.sc.seis.sod.model.event.StatefulEvent;
import edu.sc.seis.sod.model.status.Stage;
import edu.sc.seis.sod.model.status.Standing;
import edu.sc.seis.sod.model.status.Status;



public class MockECP{
    public static EventChannelPair getECP(){
        return getECP(MockStatefulEvent.create());
    }

    public static EventChannelPair getECP(Channel chan){
        return getECP(MockStatefulEvent.create(), chan);
    }

    public static EventChannelPair getECP(StatefulEvent event) {
        return getECP(event, MockChannel.createChannel());
    }

    public static EventChannelPair getECP(StatefulEvent ev, Channel chan){
        return new EventChannelPair( ev,
                                    chan, Status.get(Stage.PROCESSOR, Standing.IN_PROG),
                                    new EventStationPair(ev, (Station)chan.getSite().getStation(), Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.SUCCESS)));
    }

}

/**
 * MockFissuresUtil.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.iris.Fissures.FlinnEngdahlType;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.FlinnEngdahlRegionImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import org.easymock.MockControl;

public class MockFissuresCreator{
    public static EventAccessOperations createEvent(){
        MockControl eventController = MockControl.createControl(EventAccessOperations.class);
        EventAccessOperations mockEvent = (EventAccessOperations) eventController.getMock();
        mockEvent.get_attributes();
        eventController.setReturnValue(createEventAttr(), 3);
        try {
            mockEvent.get_preferred_origin();
        } catch (NoPreferredOrigin e) {}
        eventController.setReturnValue(createOrigin(), 3);
        eventController.replay();
        return mockEvent;
    }
    
    public static Channel createChannel(){
        return new ChannelImpl(createChanID(), "Test Channel", null, null, null, null);
    }
    
    public static ChannelId createChanID(){
        ChannelId chanId = new ChannelId();
        chanId.network_id = createNetworkID();
        chanId.channel_code = "BHZ";
        chanId.site_code = "TSTS";
        chanId.station_code = "TST";
        chanId.begin_time = time.getFissuresTime();
        return chanId;
    }
    
    public static NetworkId createNetworkID(){
        NetworkId mockId = new NetworkId();
        mockId.network_code = "TESTCODE";
        mockId.begin_time = time.getFissuresTime();
        return mockId;
    }
    
    public static Origin createOrigin(){
        return new OriginImpl(null, null, null, time.getFissuresTime(), loc, mags, null);
    }
    
    public static EventAttr createEventAttr(){
        return new EventAttrImpl("Test Event", new FlinnEngdahlRegionImpl(FlinnEngdahlType.from_int(1), 1));
    }
    
    private static MicroSecondDate time = new MicroSecondDate(0);
    
    private static Magnitude[] mags = { new Magnitude(null, 5, null) };
    
    private static Location loc = new Location(0, 0, null, new QuantityImpl(0, UnitImpl.KILOMETER), null);
}

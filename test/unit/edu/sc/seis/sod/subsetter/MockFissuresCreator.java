/**
 * MockFissuresUtil.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter;

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
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.FlinnEngdahlRegionImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import java.util.Calendar;
import java.util.TimeZone;
import org.easymock.MockControl;

public class MockFissuresCreator{
    public static EventAccessOperations createEvent(){
        return createEvent(createOrigin(), createEventAttr());
    }
    
    public static EventAccessOperations createFallEvent(){
        return createEvent(createWallFallOrigin(), createWallFallAttr());
    }
    
    public static EventAccessOperations createEvent(MicroSecondDate eventTime,
                                                    int magnitudeAndDepth,
                                                   int feRegion){
        Magnitude[] mags = {new Magnitude(null, magnitudeAndDepth, null) };
        return createEvent(createOrigin(eventTime, mags), createEventAttr(feRegion));
    }
    
    public static EventAccessOperations createEvent(Origin origin, EventAttr attr){
        MockControl eventController = MockControl.createControl(EventAccessOperations.class);
        EventAccessOperations mockEvent = (EventAccessOperations) eventController.getMock();
        mockEvent.get_attributes();
        eventController.setReturnValue(attr, MockControl.ZERO_OR_MORE);
        try {
            mockEvent.get_preferred_origin();
        } catch (NoPreferredOrigin e) {}
        eventController.setReturnValue(origin, MockControl.ZERO_OR_MORE);
        mockEvent.get_origins();
        Origin[] origins = { origin};
        eventController.setReturnValue(origins, MockControl.ZERO_OR_MORE);
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
        return createOrigin(time, mags);
    }
    
    public static Origin createOrigin(MicroSecondDate time, Magnitude[] mags){
        return new OriginImpl("Epoch in Central Alaska", "Test Data", "Charlie Groves", time.getFissuresTime(), loc, mags, null);
    }
    
    public static Origin createWallFallOrigin(){
        return new OriginImpl("Fall of the Berlin Wall", "Test Data", "Charlie Groves", getFallOfBerlinWall().getFissuresTime(), berlin, mags, null);
    }
    
    public static EventAttr createEventAttr(){ return createEventAttr(1); }
    
    public static EventAttr createEventAttr(int feRegion){
        return new EventAttrImpl("Test Event", new FlinnEngdahlRegionImpl(FlinnEngdahlType.from_int(1), feRegion));
    }
    
    public static EventAttr createWallFallAttr(){
        return new EventAttrImpl("Fall of the Berlin Wall Event", new FlinnEngdahlRegionImpl(FlinnEngdahlType.from_int(1), 543));
    }
    
    public static MicroSecondDate getFallOfBerlinWall(){
        //the berlin wall fell on the 13th of June, 1990
        if(wallFall == null){
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, 1990);
            cal.set(Calendar.MONTH, Calendar.JUNE);
            cal.set(Calendar.DATE, 13);
            cal.set(Calendar.HOUR_OF_DAY, 12);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));
            wallFall = new MicroSecondDate(cal.getTime());
        }
        return wallFall;
    }
    
    private static MicroSecondDate time = new MicroSecondDate(0);
    
    private static MicroSecondDate wallFall;
    
    private static Magnitude[] mags = { new Magnitude(null, 5, null) };
    
    private static Quantity zeroK = new QuantityImpl(0, UnitImpl.KILOMETER);
    
    private static Quantity tenK = new QuantityImpl(10, UnitImpl.KILOMETER);
    
    private static Location loc = new Location(0, 0, zeroK, zeroK, null);
    
    private static Location berlin = new Location(52.31f, 13.24f, tenK, tenK, null);
}

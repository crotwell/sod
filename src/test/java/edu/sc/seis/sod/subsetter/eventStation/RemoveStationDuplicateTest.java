package edu.sc.seis.sod.subsetter.eventStation;

import java.io.StringReader;
import java.util.Properties;

import junit.framework.TestCase;

import org.junit.Test;
import org.xml.sax.InputSource;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockStation;
import edu.sc.seis.sod.Args;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.hibernate.StatefulEventDB;


public class RemoveStationDuplicateTest extends TestCase {
    
    Start start;
    
    StationImpl mockOne = MockStation.createStation();
    StationImpl mockClose = MockStation.createCloseStation(mockOne);
    StationImpl mockFar = MockStation.createOtherStation();
    StatefulEvent event;
    
    @Override
    protected void setUp() throws Exception {
        Properties props = new Properties();
        props.put("fissuresUtil.database.url", "jdbc:hsqldb:mem:SodDB");
        Start s = new Start(new Args(new String[] {"-f", "<stream>","-q"}),
                            new Start.InputSourceCreator() {

                                public InputSource create() {
                                    return new InputSource(new StringReader("<sod></sod>\n"));
                                }
                            },
                            props,
                            true);

        s.setupDatabaseForUnitTests();
        NetworkDB netdb = NetworkDB.getSingleton();
        netdb.put(mockOne);
        netdb.put(mockClose);
        netdb.put(mockFar);
        SodDB sodDb = SodDB.getSingleton();
        event = new StatefulEvent(MockEventAccessOperations.createEvent(),
                                                Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                           Standing.SUCCESS));
        StatefulEventDB eventdb = StatefulEventDB.getSingleton();
        eventdb.put(event);
        EventStationPair esp = sodDb.createEventStationPair(event, mockOne);
        esp.update(Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.SUCCESS));
        ChannelImpl chan = MockChannel.createChannel(mockOne);
        netdb.put(chan);
        EventChannelPair ecp = sodDb.createEventChannelPair(event, chan, esp);
        ecp.update(Status.get(Stage.PROCESSOR, Standing.SUCCESS));
        SodDB.commit();
    }

    @Test
    public void testCloseStation() throws Exception {
        RemoveStationDuplicate rsd = new RemoveStationDuplicate(new QuantityImpl(1, UnitImpl.DEGREE));
        assertTrue("far station", rsd.accept(event, mockFar, null).isSuccess());
        assertFalse("close station", rsd.accept(event, mockClose, null).isSuccess());
    }
}

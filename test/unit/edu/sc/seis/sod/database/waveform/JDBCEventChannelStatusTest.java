/**
 * JDBCEventChannelStatusTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.database.waveform;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import java.sql.SQLException;
import junit.framework.TestCase;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;

public class JDBCEventChannelStatusTest extends TestCase{
    public JDBCEventChannelStatusTest(String name){ super(name); }

    public void setUp() throws SQLException{
        evChanStatus = new JDBCEventChannelStatus();
    }

    public void testPut() throws SQLException{
        int id = evChanStatus.put(ev, chan, inProgEvChan);
        assertEquals(id,  evChanStatus.put(ev, chan, rejectEvChan));
    }

    public void testGetAll() throws SQLException, NotFound{
        evChanStatus.put(ev, chan, inProgEvChan);
        evChanStatus.put(otherEv, chan, rejectEvChan);
        EventChannelPair[] pairs = evChanStatus.getAll();
        boolean evFound = false, evChanFound = false, evStatusFound = false,
            otherEvFound = false, otherEvChanFound = false, otherEvStatusFound = false;
        for (int i = 0; i < pairs.length; i++) {
            if(pairs[i].getEvent().equals(ev)){
                evFound = true;
                if(ChannelIdUtil.areEqual(pairs[i].getChannel().get_id(),
                                          chan.get_id())){
                    evChanFound = true;
                    if(pairs[i].getStatus().equals(inProgEvChan)){
                        evStatusFound = true;
                    }
                }
            }else if(pairs[i].getEvent().equals(otherEv)){
                otherEvFound = true;
                if(ChannelIdUtil.areEqual(pairs[i].getChannel().get_id(),
                                          chan.get_id())){
                    otherEvChanFound = true;
                    if(pairs[i].getStatus().equals(rejectEvChan)){
                        otherEvStatusFound = true;
                    }
                }

            }
        }
        assertTrue(evFound);
        assertTrue(evChanFound);
        assertTrue(evStatusFound);
        assertTrue(otherEvFound);
        assertTrue(otherEvChanFound);
        assertTrue(otherEvStatusFound);
        pairs = evChanStatus.getAll(ev);
        for (int i = 0; i < pairs.length; i++) {
            assertEquals(ev, pairs[i].getEvent());
        }
    }

    private CacheEvent ev = MockEventAccessOperations.createEvent(),
        otherEv = MockEventAccessOperations.createFallEvent();

    private Channel chan = MockChannel.createChannel();

    private Status rejectEvChan = Status.get(Stage.EVENT_CHANNEL_SUBSETTER,
                                             Standing.REJECT),
        inProgEvChan = Status.get(Stage.EVENT_CHANNEL_SUBSETTER,
                                  Standing.IN_PROG);

    private JDBCEventChannelStatus evChanStatus;
}

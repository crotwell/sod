/**
 * JDBCEventChannelStatusTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.database.waveform;

import edu.sc.seis.sod.Status;
import java.sql.SQLException;
import junit.framework.TestCase;

public class JDBCEventChannelStatusTest extends TestCase{
    public JDBCEventChannelStatusTest(String name){ super(name); }

    public void setUp() throws SQLException{
        evChanStatus = new JDBCEventChannelStatus();
    }

    public void testPut() throws SQLException{
        int id = evChanStatus.put(0,0, Status.get(Status.SPECIAL, Status.NEW));
        assertEquals(id,  evChanStatus.put(0,0, Status.get(Status.EVENT_CHANNEL_SUBSETTER,
                                                           Status.REJECT)));
    }

    private JDBCEventChannelStatus evChanStatus;
}

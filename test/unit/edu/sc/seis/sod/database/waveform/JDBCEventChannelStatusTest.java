/**
 * JDBCEventChannelStatusTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.database.waveform;

import edu.sc.seis.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.mockFissures.IfNetwork.MockChannel;
import junit.framework.TestCase;
import java.sql.SQLException;

public class JDBCEventChannelStatusTest extends TestCase{
    public JDBCEventChannelStatusTest(String name){ super(name); }
    
    public void setUp() throws SQLException{
        evChanStatus = new JDBCEventChannelStatus();
    }
    
    public void testPut() throws SQLException{
        int id = evChanStatus.put(0,0, EventChannelCondition.NEW);
        assertEquals(id,  evChanStatus.put(0,0,
                                           EventChannelCondition.SUBSETTER_FAILED));
    }
    
    private JDBCEventChannelStatus evChanStatus;
}

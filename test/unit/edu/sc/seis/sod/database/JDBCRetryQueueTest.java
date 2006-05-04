package edu.sc.seis.sod.database;

import java.sql.SQLException;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.event.JDBCEventAccess;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;

public class JDBCRetryQueueTest extends TestCase {

    public JDBCRetryQueueTest() {
        BasicConfigurator.configure(new NullAppender());
    }

    public void testRetry() throws SQLException {
        queue.retry(statusId);
        assertTrue(queue.willHaveNext());
        assertTrue(queue.hasNext());
        assertTrue(queue.hasNext());// two calls don't break nothin'
        assertEquals(statusId, queue.next());
        assertFalse(queue.hasNext());
        assertFalse(queue.hasNext());
        assertFalse(queue.willHaveNext());
    }

    public void testEmpty() throws SQLException {
        assertFalse(queue.hasNext());
        assertFalse(queue.willHaveNext());
        try {
            queue.next();
            assertTrue(false);
        } catch(SQLException e) {
            // Next blows up if no data in table
        }
    }

    public void testMaxRetries() throws SQLException {
        queue.setMaxRetries(20);
        for(int i = 0; i < 20; i++) {
            queue.retry(statusId);
            assertTrue(queue.hasNext());
            assertTrue(queue.willHaveNext());
            assertEquals(statusId, queue.next());
            assertFalse(queue.hasNext());
        }
        assertFalse(queue.willHaveNext());
        assertFalse(queue.hasNext());
    }

    public void testLastTime() throws SQLException {
        queue.setMaxRetries(20);
        queue.retry(statusId);
        assertTrue(queue.hasNext());
        queue.setLastRetryTime(ClockUtil.now());
        assertFalse(queue.willHaveNext());
        assertFalse(queue.hasNext());
    }

    public void testMinRetries() throws SQLException {
        queue.setMaxRetries(20);
        queue.setLastRetryTime(ClockUtil.now());
        queue.retry(statusId);
        assertFalse(queue.hasNext());
        queue.setMinRetries(1);
        assertTrue(queue.hasNext());
    }

    public void testMinRetryWait() throws SQLException {
        queue.setMinRetryWait(new TimeInterval(1, UnitImpl.MINUTE));
        queue.retry(statusId);
        assertTrue(queue.willHaveNext());
        assertFalse(queue.hasNext());
        queue.setMinRetryWait(new TimeInterval(0, UnitImpl.MINUTE));
        assertTrue(queue.willHaveNext());
        assertTrue(queue.hasNext());
    }

    public void testEventDataLag() throws SQLException {
        queue.retry(statusId);
        assertTrue(queue.hasNext());
        queue.setEventDataLag(new TimeInterval(0, UnitImpl.SECOND));
        assertFalse(queue.hasNext());
        assertFalse(queue.willHaveNext());
    }

    public void setUp() throws SQLException {
        ConnMgr.setURL("jdbc:hsqldb:.");
        JDBCEventChannelStatus status = new JDBCEventChannelStatus();
        JDBCEventAccess events = new JDBCEventAccess();
        int evId = events.put(MockEventAccessOperations.createEvent(ClockUtil.now(),
                                                                    7,
                                                                    7),
                              null,
                              null,
                              null);
        statusId = status.put(evId,
                              1,
                              Status.get(Stage.AVAILABLE_DATA_SUBSETTER,
                                         Standing.RETRY));
        queue = new JDBCRetryQueue("test");
    }

    public void tearDown() throws SQLException {
        ConnMgr.createConnection().createStatement().execute("SHUTDOWN");
    }

    private int statusId;

    private JDBCRetryQueue queue;
}

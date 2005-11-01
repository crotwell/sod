package edu.sc.seis.sod.database;

import java.sql.SQLException;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.ConnMgr;

public class JDBCRetryQueueTest extends TestCase {

    public JDBCRetryQueueTest() {
        BasicConfigurator.configure();
    }

    public void testRetry() throws SQLException {
        queue.retry(1);
        assertTrue(queue.willHaveNext());
        assertTrue(queue.hasNext());
        assertTrue(queue.hasNext());// two calls don't break nothin'
        assertEquals(queue.next(), 1);
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
            queue.retry(1);
            assertTrue(queue.hasNext());
            assertTrue(queue.willHaveNext());
            assertEquals(1, queue.next());
            assertFalse(queue.hasNext());
        }
        assertFalse(queue.willHaveNext());
        assertFalse(queue.hasNext());
    }

    public void testLastTime() throws SQLException {
        queue.setMaxRetries(20);
        queue.retry(1);
        assertTrue(queue.hasNext());
        queue.setLastRetryTime(ClockUtil.now());
        assertFalse(queue.willHaveNext());
        assertFalse(queue.hasNext());
    }

    public void testMinRetries() throws SQLException {
        queue.setMaxRetries(20);
        queue.setLastRetryTime(ClockUtil.now());
        queue.retry(1);
        assertFalse(queue.hasNext());
        queue.setMinRetries(1);
        assertTrue(queue.hasNext());
    }

    public void testMinRetryWait() throws SQLException {
        queue.setMinRetryWait(new TimeInterval(1, UnitImpl.MINUTE));
        queue.retry(1);
        assertTrue(queue.willHaveNext());
        assertFalse(queue.hasNext());
        queue.setMinRetryWait(new TimeInterval(0, UnitImpl.MINUTE));
        assertTrue(queue.willHaveNext());
        assertTrue(queue.hasNext());
    }

    public void setUp() throws SQLException {
        queue = new JDBCRetryQueue("test");
    }

    public void tearDown() throws SQLException {
        ConnMgr.createConnection().createStatement().execute("SHUTDOWN");
    }

    private JDBCRetryQueue queue;
}

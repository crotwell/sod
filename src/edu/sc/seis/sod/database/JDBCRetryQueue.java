package edu.sc.seis.sod.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.TimeUtils;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;

public class JDBCRetryQueue extends JDBCTable {

    public JDBCRetryQueue(String queueName) throws SQLException {
        super(queueName + "RetryQueue", ConnMgr.createConnection());
        TableSetup.setup(this,
                         "edu/sc/seis/sod/database/props/retryqueue.vm");
    }

    /**
     * returns true ifan item will be returned by next without respect to the
     * minRetryWait
     */
    public synchronized boolean willHaveNext() throws SQLException {
        fillInWillHaveParams(willHaveNext, 1);
        return willHaveNext.executeQuery().next();
    }

    /**
     * returns true if an item will be returned by next
     */
    public synchronized boolean hasNext() throws SQLException {
        int curPos = fillInWillHaveParams(hasNext, 1);
        MicroSecondDate now = ClockUtil.now();
        hasNext.setTimestamp(curPos, now.subtract(minRetryWait).getTimestamp());
        return hasNext.executeQuery().next();
    }

    private int fillInWillHaveParams(PreparedStatement ps, int startPos)
            throws SQLException {
        ps.setTimestamp(startPos++, beingRetried);
        if(ClockUtil.now().before(lastRetry)){
            ps.setInt(startPos++, maxRetries);
        }else{
            ps.setInt(startPos++, minRetries);
        }
        return startPos;
    }

    /**
     * Sets the last time at which items in the queue with retries that have
     * been tried more than the min but less than the max will be retried again
     * 
     * defaults to TimeUtils.future
     */
    public void setLastRetryTime(MicroSecondDate lastTimeToRetry) {
        lastRetry = lastTimeToRetry;
    }

    /**
     * Sets the minimum number of times something will be retried even if the
     * last retry time has been reached
     * 
     * defaults to 0
     */
    public void setMinRetries(int min) {
        minRetries = min;
    }

    /**
     * Sets the maximum number of times an item will be retried. If last retry
     * time is reached before this number is reached, it isn't retried.
     * 
     * defaults to 1
     */
    public void setMaxRetries(int max) {
        maxRetries = max;
    }

    /**
     * Sets the minimum time a query will come up in next after a retry
     * 
     * defaults to no time at all
     */
    public void setMinRetryWait(TimeInterval wait) {
        minRetryWait = wait;
    }

    /**
     * Inserts an object in the queue to be retried. If the item is already in
     * the queue its last retry time is updated to now
     */
    public synchronized void retry(int id) throws SQLException {
        get.setInt(1, id);
        PreparedStatement toUse = insert;
        if(get.executeQuery().next()) {
            toUse = updateRetry;
        }
        toUse.setTimestamp(1, ClockUtil.now().getTimestamp());
        toUse.setInt(2, id);
        toUse.executeUpdate();
    }

    public String toString() {
        return getTableName();
    }

    /**
     * returns the next item to be retried. Should only be called after hasNext
     * has been called or if there is no next, an SQLException will be thrown.
     * 
     * this item won't be returned from next unless retry is called again
     */
    public synchronized int next() throws SQLException {
        next.setInt(1, maxRetries);
        ResultSet rs = next.executeQuery();
        rs.next();
        updateNext.setTimestamp(1, beingRetried);
        int id = rs.getInt("id");
        updateNext.setInt(2, id);
        updateNext.executeUpdate();
        return id;
    }

    private int maxRetries = 1, minRetries = 0;

    private MicroSecondDate lastRetry = TimeUtils.future;

    private Timestamp beingRetried = TimeUtils.futurePlusOne.getTimestamp();

    private TimeInterval minRetryWait = new TimeInterval(0, UnitImpl.SECOND);

    private PreparedStatement next, hasNext, get, insert, updateNext,
            updateRetry, willHaveNext;
}

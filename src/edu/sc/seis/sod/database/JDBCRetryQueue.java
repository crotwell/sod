package edu.sc.seis.sod.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
        TableSetup.setup(this, "edu/sc/seis/sod/database/props/retryqueue.vm");
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
        if(retries.size() > 0) {
            return true;
        }
        if(nextHasNextCheck.after(ClockUtil.now())) {
            return false;
        }
        int curPos = fillInWillHaveParams(hasNext, 1);
        MicroSecondDate now = ClockUtil.now();
        hasNext.setTimestamp(curPos, now.subtract(minRetryWait).getTimestamp());
        ResultSet rs = hasNext.executeQuery();
        retries = new ArrayList();
        while(rs.next()) {
            retries.add(new Integer(rs.getInt("id")));
        }
        if(retries.size() == 0) {
            nextHasNextCheck = ClockUtil.now().add(hasNextWaitOnEmpty);
        }
        return retries.size() > 0;
    }

    private int fillInWillHaveParams(PreparedStatement ps, int startPos)
            throws SQLException {
        ps.setTimestamp(startPos++, beingRetried);
        if(ClockUtil.now().before(lastRetry)) {
            ps.setInt(startPos++, maxRetries);
        } else {
            ps.setInt(startPos++, minRetries);
        }
        ps.setTimestamp(startPos++, ClockUtil.now()
                .subtract(eventDataLag)
                .getTimestamp());
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
        retries = new ArrayList();
    }

    /**
     * Sets the minimum number of times something will be retried even if the
     * last retry time has been reached
     * 
     * defaults to 0
     */
    public void setMinRetries(int min) {
        minRetries = min;
        retries = new ArrayList();
    }

    /**
     * Sets the maximum number of times an item will be retried. If last retry
     * time is reached before this number is reached, it isn't retried.
     * 
     * defaults to 1
     */
    public void setMaxRetries(int max) {
        maxRetries = max;
        retries = new ArrayList();
    }

    /**
     * Sets the minimum time a query will come up in next after a retry
     * 
     * defaults to no time at all
     */
    public void setMinRetryWait(TimeInterval wait) {
        minRetryWait = wait;
        retries = new ArrayList();
    }

    /**
     * Sets the amount of time after an event an item will be retried.
     * 
     * For available data it makes sense to make this as long as you expect data
     * to show up past the event's time in the server. For corba failures,
     * making this a huge amount and letting the min and max retries take care
     * of a retry makes more sense
     * 
     * defaults to 1000 years
     */
    public void setEventDataLag(TimeInterval lag) {
        eventDataLag = lag;
        retries = new ArrayList();
    }

    public void setHasNextWaitOnEmpty(TimeInterval wait) {
        hasNextWaitOnEmpty = wait;
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
     * has been called
     * 
     * this item won't be returned from next unless retry is called again
     * 
     * @exception NoSuchElementException
     *                if there are no more retries
     */
    public synchronized int next() throws SQLException {
        try {
            int id = ((Integer)retries.remove(0)).intValue();
            updateNext.setTimestamp(1, beingRetried);
            updateNext.setInt(2, id);
            updateNext.executeUpdate();
            return id;
        } catch(IndexOutOfBoundsException e) {
            throw new NoSuchElementException("No more retries!");
        }
    }

    private int maxRetries = 1, minRetries = 0;

    private MicroSecondDate lastRetry = TimeUtils.future;

    private Timestamp beingRetried = TimeUtils.futurePlusOne.getTimestamp();

    private TimeInterval minRetryWait = new TimeInterval(0, UnitImpl.SECOND);

    private TimeInterval eventDataLag = new TimeInterval(1000 * 52,
                                                         UnitImpl.WEEK);

    private TimeInterval hasNextWaitOnEmpty = new TimeInterval(5,
                                                               UnitImpl.MINUTE);

    private PreparedStatement hasNext, get, insert, updateNext, updateRetry,
            willHaveNext;

    private List retries = new ArrayList();

    private MicroSecondDate nextHasNextCheck = ClockUtil.now();
}

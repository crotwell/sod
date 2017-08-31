package edu.sc.seis.sod;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import edu.sc.seis.sod.hibernate.ConnMgr;
import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.util.time.ClockUtil;

/**
 * does a checkpoint on the database every hour
 * 
 * @author groves Created on May 9, 2005
 */
public class PeriodicCheckpointer extends TimerTask {

    public PeriodicCheckpointer() {
        if (ConnMgr.getDB_TYPE().equals(ConnMgr.HSQL)) {
        Timer t = new Timer(true);
        t.schedule(this,
                   Date.from(ClockUtil.now().plus(Duration.ofHours(1))),
                   ONE_HOUR);
        } else {
            logger.warn("Checkpointing only makes sense for HSQLDB, not "+ConnMgr.getDB_TYPE());
        }
    }

    public void run() {
        try {
            Connection conn = ConnMgr.createConnection();
            Statement stmt = conn.createStatement();
            logger.info("Checkpointing db");
            stmt.executeUpdate("CHECKPOINT");
            logger.info("Done checkpointing db");
            conn.close();
        } catch(SQLException e) {
            GlobalExceptionHandler.handle(e);
        }
    }

    private static final int ONE_HOUR = 60 * 60 * 1000;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PeriodicCheckpointer.class);
}

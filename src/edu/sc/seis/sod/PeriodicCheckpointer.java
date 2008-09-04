package edu.sc.seis.sod;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * does a checkpoint on the database every hour
 * 
 * @author groves Created on May 9, 2005
 */
public class PeriodicCheckpointer extends TimerTask {

    public PeriodicCheckpointer() {
        Timer t = new Timer(true);
        t.schedule(this,
                   new MicroSecondDate().add(new TimeInterval(1, UnitImpl.HOUR)),
                   ONE_HOUR);
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

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PeriodicCheckpointer.class);
}

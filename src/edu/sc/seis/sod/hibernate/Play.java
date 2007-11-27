package edu.sc.seis.sod.hibernate;

import java.sql.SQLException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.dialect.function.SQLFunctionTemplate;

import edu.sc.seis.fissuresUtil.hibernate.HibernateUtil;
import edu.sc.seis.fissuresUtil.simple.TimeOMatic;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.mock.MockECP;


public class Play extends edu.sc.seis.fissuresUtil.hibernate.Play {
    
    static {
        HibernateUtil.getConfiguration()
        .addResource("edu/sc/seis/sod/hibernate/sod.hbm.xml")
        .addSqlFunction( "datediff", new SQLFunctionTemplate(Hibernate.LONG, "datediff(?1, ?2, ?3)" ) );
    }

    public static void main(String[] args) throws SQLException {
        try {
            BasicConfigurator.configure();
            Logger.getRootLogger().setLevel(Level.INFO);
            Play mgr = new Play();
            TimeOMatic.start();
            String todo = args[2];
            System.out.println("arg is: " + todo);
            if ( ! mgr.doIt(todo)) {
                System.err.println("Unknown arg: " + todo);
            }
            TimeOMatic.print("end");
        } catch (Throwable t) {
            logger.error("big problem!", t);
        }
    }
    protected boolean doIt(String todo) throws Exception {
        if (super.doIt(todo)) {
            return true;
        }
        if (todo.equals("storeecp")) {
            storeECP();
        } else {
            return false;
        }
        return true;
    }
    
    protected void storeECP() {
        SodDB sodDb = new SodDB();
        EventChannelPair ecp = MockECP.getECP();
        sodDb.put(ecp);
        sodDb.commit();
        sodDb.getSession().lock(ecp, LockMode.NONE);
    }
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Play.class);
}

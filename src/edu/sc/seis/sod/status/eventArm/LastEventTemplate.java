package edu.sc.seis.sod.status.eventArm;

import java.sql.SQLException;
import org.w3c.dom.Element;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.event.JDBCEventAccess;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.AllTypeTemplate;
import edu.sc.seis.sod.status.EventFormatter;

public class LastEventTemplate extends AllTypeTemplate {

    public LastEventTemplate(Element el) throws ConfigurationException {
        ef = new EventFormatter(el);
        try {
            evAcc = new JDBCEventAccess();
        } catch(SQLException e) {
            throw new ConfigurationException("Couldn't create database to get last event",
                                             e);
        }
    }

    public String getResult() {
        try {
            CacheEvent ev = evAcc.getLastEvent();
            return ef.getResult(ev);
        } catch(NotFound e) {
            return "None";
        } catch(SQLException e) {
            GlobalExceptionHandler.handle(e);
            return "Database Error!";
        }
    }

    private JDBCEventAccess evAcc;

    private EventFormatter ef;
}
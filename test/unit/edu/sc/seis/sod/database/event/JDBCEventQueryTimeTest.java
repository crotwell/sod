package edu.sc.seis.sod.database.event;

import java.sql.SQLException;
import java.util.Date;
import junit.framework.TestCase;
import edu.sc.seis.fissuresUtil.database.NotFound;

public class JDBCEventQueryTimeTest extends TestCase{
    public JDBCEventQueryTimeTest(String name){
        super(name);
        
    }
    
    public void testGetTime() throws SQLException{
        JDBCEventQueryTime queryTimes = new JDBCEventQueryTime();
        Date startTime = new Date(0);
        Date endTime = new Date(3212131);
        String server = "TESTSERVER";
        String dns = "TEST/DNS";
        queryTimes.setTimes(server, dns, startTime, endTime);
        assertEquals(startTime, queryTimes.getStart(server, dns));
        assertEquals(endTime, queryTimes.getEnd(server, dns));
    }
}

package edu.sc.seis.sod.database.waveform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.sod.database.SodJDBC;

public class JDBCEventRecordSection extends JDBCTable {

    static {
        ConnMgr.addPropsLocation("edu/sc/seis/sod/database/props/");
    }

    public JDBCEventRecordSection() throws SQLException {
        this(ConnMgr.createConnection());
    }

    public JDBCEventRecordSection(Connection conn) throws SQLException {
        super("eventrecordsection", conn);
        if(!DBUtil.tableExists("eventrecordsection", conn)) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(ConnMgr.getSQL("eventrecordsection.create"));
        }
        seq = new JDBCSequence(conn, "RecordSectionSeq");
        prepareStatements();
    }
    public void insert(int eventid,String imageName,boolean bestForEvent) throws SQLException {
        insert.setInt(1, seq.next());
        insert.setInt(2, eventid);
        insert.setString(3,imageName);
        insert.setBoolean(4,bestForEvent);
        insert.executeUpdate();
    }
    public String getBestImageforEvent(int eventid) throws SQLException{
        getBestImageforEvent.setInt(1,eventid);
        try {
            ResultSet rs = getBestImageforEvent.executeQuery();
            rs.next();
            return rs.getString(1);
        } catch(SQLException e) {
            throw new RuntimeException("Running the SQL statement" + getBestImageforEvent.toString(), e);
        }
    }
    public String getImageforEventChannel(int eventid,int channelid) throws SQLException{
        getImageforEventChannel.setInt(1,eventid);
        getImageforEventChannel.setInt(2,channelid);
        try {
            ResultSet rs = getImageforEventChannel.executeQuery();
            rs.next();
            return rs.getString(1);
        } catch(SQLException e) {
            throw new RuntimeException("Running the SQL statement " + getImageforEventChannel.toString(), e);
        }
    }

    private PreparedStatement getBestImageforEvent, insert,
            getImageforEventChannel;
    
    private JDBCSequence seq;
}
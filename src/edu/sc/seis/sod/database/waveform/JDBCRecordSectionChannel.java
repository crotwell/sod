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

public class JDBCRecordSectionChannel extends JDBCTable {

    static {
        ConnMgr.addPropsLocation("edu/sc/seis/sod/database/props/");
    }

    public JDBCRecordSectionChannel() throws SQLException {
        this(ConnMgr.createConnection());
    }

    public JDBCRecordSectionChannel(Connection conn) throws SQLException {
        super("recordsectionchannel", conn);
        if(!DBUtil.tableExists("recordsectionchannel", conn)) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(ConnMgr.getSQL("recordsectionchannel.create"));
        }
        prepareStatements();
    }

    public void setRecSecId(int newRecSecId, int eventId, int channelId)
            throws SQLException {
        
    }

    public void insert(int recSecId, int channelid) throws SQLException {
        insert.setInt(1, recSecId);
        insert.setInt(2, channelid);
        insert.executeUpdate();
    }

    public void updateRecordSection(int newRecSecId, int eventId, int channelId)
            throws SQLException {
        try {
            int curRecSecId = getRecSecId(eventId, channelId);
            System.out.println("Old recSecId = " +curRecSecId + " for " +channelId  );
            updateRecordSection.setInt(1, newRecSecId);
            updateRecordSection.setInt(2, curRecSecId);
            updateRecordSection.executeUpdate();
        } catch(SQLException e) {
            throw new RuntimeException("Running the SQL query"
                    + updateRecordSection.toString());
        }
    }
    public boolean channelExists(int eventId,int channelId)throws SQLException{
     if(getRecSecId(eventId,channelId)!= -1) {
         return true;
     }
     return false;
    }
    public int getRecSecId(int eventId, int channelId) throws SQLException {
        getRecSecId.setInt(1, eventId);
        getRecSecId.setInt(2, channelId);
        try {
            ResultSet rs = getRecSecId.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch(SQLException e) {
       return -1;
        }
    }

    PreparedStatement insert, updateRecordSection, getRecSecId;
}
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
    public JDBCRecordSectionChannel(Connection conn) throws SQLException{
        super("recordsectionchannel", conn);
        if(!DBUtil.tableExists("recordsectionchannel", conn)) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(ConnMgr.getSQL("recordsectionchannel.create"));
        }
        prepareStatements();
    }
    public void insert(int recSecId,int channelid) throws SQLException {
        insert.setInt(1, recSecId);
        insert.setInt(2, channelid);
        insert.executeUpdate();
    }
    public void updateRecordSection(int recSecId,int eventid,int channelid) throws SQLException{
        updateRecordSection.setInt(1,recSecId);
        updateRecordSection.setInt(2,eventid);
        updateRecordSection.setInt(3,channelid);
        updateRecordSection.executeUpdate();
    }
    
    PreparedStatement insert,updateRecordSection;
}
/**
 * JDBCVersion.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.database;

import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.sod.Version;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCVersion extends SodJDBC{

    public JDBCVersion() throws SQLException{
        conn = ConnMgr.createConnection();
        if (!DBUtil.tableExists("version", conn)){
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(ConnMgr.getSQL("version.create"));
            stmt.executeUpdate("INSERT INTO version (dbversion) values ('"
                                   + Version.getVersion()
                                   +"')");
        }
        getVersion = prepare("SELECT dbversion FROM version");
    }

    public String getDBVersion() throws SQLException, NotFound{
        ResultSet rs = getVersion.executeQuery();
        if (rs.next()){
            String val = rs.getString("dbversion");
            return val;
        }
        throw new NotFound("There is no version stored in the database");
    }

    private PreparedStatement prepare(String query) throws SQLException{
        return conn.prepareStatement(query);
    }

    private Connection conn;

    private PreparedStatement getVersion;
}


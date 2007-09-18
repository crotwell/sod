/**
 * JDBCVersion.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;
import edu.sc.seis.sod.Version;

public class JDBCVersion extends JDBCTable {

    public JDBCVersion() throws SQLException{
        super("version", ConnMgr.createConnection());
        TableSetup.setup(this, "edu/sc/seis/sod/database/props/default.props");
        synchronized(TableSetup.class) {
            getConnection().createStatement().executeUpdate("INSERT INTO version (dbversion) values ('"
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

    private PreparedStatement getVersion;
}


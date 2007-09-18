/**
 * JDBCStatus.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;
import edu.sc.seis.sod.Status;

public class JDBCStatus extends JDBCTable {

    public JDBCStatus() throws SQLException {
        super("status", ConnMgr.createConnection());
        TableSetup.setup(this, "edu/sc/seis/sod/database/props/default.props");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select * from "+getTableName());
        
        if( ! rs.next()) {
            // result set is empty, so add status
            Status[][] all = Status.ALL;
            for(int i = 0; i < all.length; i++) {
                for(int j = 0; j < all[i].length; j++) {
                    stmt.executeUpdate("INSERT INTO status( id, name ) values ( "
                            + all[i][j].getAsShort()
                            + ", '"
                            + all[i][j].toString() + "' )");
                }
            }
        }
    }
}

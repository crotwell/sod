/**
 * JDBCStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.Status;

public class JDBCStatus{
    public JDBCStatus(){
        try {
            Connection conn = ConnMgr.createConnection();
            Statement stmt = conn.createStatement();
            if(!DBUtil.tableExists("status", conn)){
                stmt.executeUpdate("CREATE TABLE status ( id int, name varchar )");
            }
            Status[][] all = Status.ALL;
            for (int i = 0; i < all.length; i++) {
                for (int j = 0; j < all[i].length; j++) {
                    stmt.executeUpdate("INSERT INTO status( id, name ) values ( " + all[i][j].getAsShort() + ", '" + all[i][j].toString() + "' )");
                }
            }
        } catch (SQLException e) {
            GlobalExceptionHandler.handle(e);
        }

    }
}


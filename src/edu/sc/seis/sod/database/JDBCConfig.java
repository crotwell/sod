/**
 * JDBCConfig.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.database;

import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.xml.sax.InputSource;

public class JDBCConfig extends SodJDBC {

    public JDBCConfig(String config) throws SQLException, IOException{
        conn = ConnMgr.createConnection();
        if (!DBUtil.tableExists("config", conn)){
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(ConnMgr.getSQL("config.create"));
            stmt.executeUpdate("INSERT INTO config (configString) values ('"
                                   + config
                                   + "')");
        }
        getConfig = prepare("SELECT configString FROM config");
    }

    public boolean isSameConfig(String config) throws NotFound, SQLException{
        ResultSet rs = getConfig.executeQuery();
        if (rs.next()){
            String val = rs.getString("configString");
            return val.equals(config);
        }
        throw new NotFound("There is no config stored in the database");
    }

    public static String getConfigString(File configFile) throws IOException{
        return getConfigString(new BufferedReader(new FileReader(configFile)));
    }

    public static String getConfigString(InputSource is) throws IOException{
        InputStreamReader isr = new InputStreamReader(is.getByteStream());
        return getConfigString(new BufferedReader(isr));
    }

    private static String getConfigString(BufferedReader r1) throws IOException{
        StringBuffer buf = new StringBuffer();
        String line;
        while((line = r1.readLine()) != null){ buf.append(line); }
        r1.close();
        return buf.toString();
    }

    private PreparedStatement prepare(String query) throws SQLException{
        return conn.prepareStatement(query);
    }

    private Connection conn;
    private PreparedStatement getConfig;
}


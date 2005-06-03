/**
 * JDBCConfig.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.database;

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
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;

public class JDBCConfig extends SodJDBC {

    public JDBCConfig(Connection conn) throws SQLException, IOException {
        this.conn = conn;
        if(!DBUtil.tableExists("config", conn)) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(ConnMgr.getSQL("config.create"));
        }
        getConfig = prepare("SELECT configString FROM config");
    }

    public JDBCConfig(String config, boolean update) throws SQLException,
            IOException {
        this(ConnMgr.createConnection());
        Statement stmt = conn.createStatement();
        try {
            if(!isSameConfig(config)) {
                stmt.executeUpdate("UPDATE config SET configString = '"
                        + escapeConfigString(config) + "'");
            }
        } catch(NotFound e) {
            // database is empty, so insert
            stmt.executeUpdate("INSERT INTO config (configString) values ('"
                    + escapeConfigString(config) + "')");
        }
    }

    public String getCurrentConfig() throws NotFound, SQLException {
        ResultSet rs = getConfig.executeQuery();
        if(rs.next()) {
            String val = rs.getString("configString");
            return unEscapeConfigString(val);
        }
        throw new NotFound("There is no config stored in the database");
    }

    public boolean isSameConfig(String config) throws NotFound, SQLException {
        String val = getCurrentConfig();
        return val.equals(config);
    }

    public static String extractConfigString(File configFile)
            throws IOException {
        return extractConfigString(new BufferedReader(new FileReader(configFile)));
    }

    public static String extractConfigString(InputSource is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is.getByteStream());
        return extractConfigString(new BufferedReader(isr));
    }

    private static String extractConfigString(BufferedReader r1)
            throws IOException {
        StringBuffer buf = new StringBuffer();
        String line;
        while((line = r1.readLine()) != null) {
            buf.append(line);
        }
        r1.close();
        return buf.toString();
    }
    
    private static String escapeConfigString(String config) {
        return config.replaceAll("'", SINGLE_QUOTE);
    }
    
    private static String unEscapeConfigString(String config) {
        return config.replaceAll(SINGLE_QUOTE, "'");
    }

    private PreparedStatement prepare(String query) throws SQLException {
        return conn.prepareStatement(query);
    }

    private Connection conn;

    private PreparedStatement getConfig;
    
    private static String SINGLE_QUOTE = "#SINGLE_QUOTE#";
}
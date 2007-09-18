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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.xml.sax.InputSource;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;

public class JDBCConfig extends JDBCTable {

    public JDBCConfig(Connection conn) throws SQLException, IOException {
        super("config",  conn);
        TableSetup.setup(this, "edu/sc/seis/sod/database/props/default.props");
        getConfig = prepare("SELECT configString FROM config");
    }

    public JDBCConfig(String config, boolean update) throws SQLException,
            IOException {
        this(ConnMgr.createConnection());
        Statement stmt = conn.createStatement();
        try {
            if(!isSameConfig(config) && update) {
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
        return getCurrentConfig().equals(config);
    }

    public static String extractConfigString(File configFile)
            throws IOException {
        return extractConfigString(new BufferedReader(new FileReader(configFile)));
    }

    public static String extractConfigString(InputSource is) throws IOException {
        return extractConfigString(new BufferedReader(is.getCharacterStream()));
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

    private PreparedStatement getConfig;
    
    private static String SINGLE_QUOTE = "#SINGLE_QUOTE#";
}
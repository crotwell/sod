package edu.sc.seis.sod.database;

import edu.sc.seis.fissuresUtil.database.ConnMgr;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.apache.log4j.Category;
/**
 * HSqlDbManager.java
 *
 *
 * Created: Thu Oct  3 14:44:46 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class HSqlDbManager extends AbstractDatabaseManager{
    public HSqlDbManager (Properties props){
        super(props);
        //here set the log size and if possible even the size of the script file.
        //setLogSize();
    }
    
    public NetworkDatabase getNetworkDatabase(){
        if(networkDatabase == null) {
            try {
                networkDatabase = new HSqlNetworkDb(ConnMgr.createConnection());
            } catch (SQLException e) {
                throw new RuntimeException("Trouble setting up network db", e);
            }
        }
        return networkDatabase;
    }
    
    public Connection getConnection() {
        try {
            if(connection == null) {
                String driverName = new String("org.hsqldb.jdbcDriver");
                Class.forName(driverName).newInstance();
                System.out.println("The database name is "+getDatabaseName());
                connection = ConnMgr.createConnection();
                //DriverManager.getConnection("jdbc:hsqldb:"+getDatabaseName(), "sa", "");
                
            }
            return connection;
        } catch(Exception sqle) {
            //      sqle.printStackTrace();
            logger.debug("Unable to create the connection to HSQLDB database "+getDatabaseName(),sqle);
            return null;
        }
        
    }
    
    private void setLogSize() {
        try {
            Connection connection = getConnection();
            if(connection == null) return;
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("SET LOGSIZE 10");
        } catch(SQLException sqle) {
            //error cannot set the logsize.
            //sqle.printStackTrace();
            logger.debug("ERROR: unable to set the log size ");
        }
    }
    
    private ConfigDatabase eventConfigDb;
    
    private NetworkDatabase networkDatabase;
    
    private Connection connection;
    
    private static Category logger = Category.getInstance(HSqlDbManager.class.getName());
    
}// HSqlDbManager

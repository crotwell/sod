package edu.sc.seis.sod.database;

import java.sql.*;
import java.util.*;

/**
 * PostgresDbManager.java
 *
 *
 * Created: Thu Oct  3 14:36:14 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class PostgresDbManager extends AbstractDatabaseManager{
    public PostgresDbManager (Properties props){
        super(props);
    }
    
    public NetworkDatabase getNetworkDatabase() {
        if(networkDatabase == null) {
            networkDatabase = new PostgresNetworkDb(getConnection());
        }
        return networkDatabase;
    }
    
    public Connection getConnection() {
        try {
            if(connection == null) {
                isDatabaseExists();
                connection = DriverManager.getConnection("jdbc:postgresql:"+getDatabaseName(),
                                                         getUserName(), "");
            }
            return connection;
        } catch(Exception sqle) {
            sqle.printStackTrace();
            return null;
        }
    }
    
    public boolean isDatabaseExists() {
        
        String driverName = new String("org.postgresql.Driver");
        try {
            Driver driver = (Driver)Class.forName(driverName).newInstance();
            Properties props = new Properties();
            props.setProperty("user",getUserName());
            props.setProperty("password", "");
            driver.connect("jdbc:postgresql:"+getDatabaseName(), props);
            return true;
        } catch(Exception sqle){
            createDatabase();
            return false;
        }
    }
    
    private void createDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:postgresql:template1",
                                                     getUserName(),
                                                     "");
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("create database "+getDatabaseName());
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
        
    }
    
    private ConfigDatabase eventConfigDb;
    
    private NetworkDatabase networkDatabase;
    
    private Connection connection;
    
}// PostgresDbManager

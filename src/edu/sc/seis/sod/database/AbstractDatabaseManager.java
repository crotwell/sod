package  edu.sc.seis.sod.database;

import java.util.*;
import java.sql.*;
/**
 * AbstractDatabaseManager.java
 *
 *
 * Created: Thu Oct  3 15:15:52 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public  abstract class AbstractDatabaseManager  {
    public AbstractDatabaseManager (Properties props){
    this.props = props;
    }




    public abstract Connection getConnection();

    public abstract ConfigDatabase getConfigDatabase();

    public abstract EventDatabase getEventDatabase();

    public abstract NetworkDatabase getNetworkDatabase();

    public abstract WaveformDatabase getWaveformDatabase();

    public void close() {
    try {
        //    getEventDatabase().delete(Status.COMPLETE_SUCCESS);
        getConnection().close();
    } catch(SQLException sqle) {
        sqle.printStackTrace();
    }
    }
    public String getDatabaseName() {
    String value = props.getProperty("edu.sc.seis.sod.databasename");
    if(value == null) value = "SodDb";

    return value;
    }

    public String getUserName() {
    String value = props.getProperty("edu.sc.seis.sod.username");
    if(value == null) value = "sod";

    return value;
    }

  //   public void checkRestartOptions() {

//  //get the quitTime
//  //get refresh Time.
//  //first check if the database alread exists.
//  if(isDatabaseExists()) AbstractDatabaseManager.DATABASE_EXISTS_INITIALLY = true;
//  if(isRemoveDatabase()) AbstractDatabaseManager.REMOVE_DATABASE = true;
//  if(isRefreshInterval()) AbstractDatabaseManager.GET_NEW_EVENTS = true;
//     }

//     private boolean isRemoveDatabase() {
//  if(props.getProperty("edu.sc.seis.sod.database.remove") == null) return false;
//  else return true;
//     }

//     private boolean isRefreshInterval() {
//  if(props.getProperty("edu.sc.seis.sod.database.eventRefreshInterval") == null) return true;
//  else return false;
//     }

    private Properties props = null;

//     protected static boolean DATABASE_EXISTS_INITIALLY = false;

//     protected static boolean GET_NEW_EVENTS = false;

//     protected static boolean GET_NEW_CHANNELS = false;

//     protected static boolean REMOVE_DATABASE = false;


}// AbstractDatabaseManager

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

    public void close() {
	try {
	    getEventDatabase().delete(Status.COMPLETE_SUCCESS);
	    getConnection().close();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }
    public String getDatabaseName() {
	String value = props.getProperty("edu.sc.seis.sod.databasename");
	if(value == null) value = "sodDatabase";

	return value;
    }
    
    public String getUserName() {
	String value = props.getProperty("edu.sc.seis.sod.username");
	if(value == null) value = "sod";

	return value;
    }

   

    private Properties props = null;

  
}// AbstractDatabaseManager

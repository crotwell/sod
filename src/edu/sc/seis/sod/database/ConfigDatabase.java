
package edu.sc.seis.sod.database;

import edu.iris.Fissures.model.*;

import java.io.*;
import java.util.*;
import java.sql.*;


/**
 * ConfigDatabase.java
 *
 *
 * Created: Thu Sep 12 15:33:59 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public interface ConfigDatabase {
       
    public void setTime(String serverName,
			String serverDNS,
			edu.iris.Fissures.Time time);
    
    // public void setServerDNS(String serverDNS);

    //     public void setServerName(String serverName);

    public edu.iris.Fissures.Time getTime(String serverName, 
					  String serverDNS);
    
    public void incrementTime(String serverName,
			      String serverDNS, 
			      int days);

    public void clean();

    public String getTableName();
   
    // public String getServerDNS();

//     public String getServerName();

}// ConfigDatabase

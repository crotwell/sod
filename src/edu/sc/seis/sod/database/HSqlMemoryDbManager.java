package edu.sc.seis.sod.database;

import java.sql.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * HSqlMemoryDbManager.java
 *
 *
 * Created: Fri Mar 21 15:42:36 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class HSqlMemoryDbManager extends HSqlDbManager{
    public HSqlMemoryDbManager (Properties props){
        super(props);
    }
    
    public String getDatabaseName() {
        return ".";
    }
    
}// HSQLMemoryDbManager

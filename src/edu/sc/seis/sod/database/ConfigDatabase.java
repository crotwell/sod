package edu.sc.seis.sod.database;

import edu.iris.Fissures.model.MicroSecondDate;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;

public abstract class ConfigDatabase{
    public ConfigDatabase (Connection connection, String tableName){
        this.connection = connection;
        this.tableName = tableName;
        init();
    }

    public abstract void create();

    private void init() {
        try {
            create();
            setTimeStmt = connection.prepareStatement(" INSERT into  "+tableName+
                                                          " VALUES(?, ? , ? ) ");
            updateTimeStmt = connection.prepareStatement(" UPDATE "+tableName+" set time = ? "+
                                                             " WHERE serverName = ? AND "+
                                                             " serverDNS = ? ");

            getTimeStmt = connection.prepareStatement(" SELECT time from "+tableName+
                                                          " WHERE serverName = ? AND "+
                                                          " serverDNS = ? ");
            deleteStmt = " DELETE FROM ";
        } catch(Exception e) {
            edu.sc.seis.sod.CommonAccess.handleException("Problem in init", e);
        }


    }
    public ConfigDatabase(edu.iris.Fissures.Time time,
                                  String serverDNS,
                                  String serverName) {

        setTime(serverName,
                serverDNS,
                time);
    }


    public synchronized void setTime(String serverName,
                                     String serverDNS,
                                     edu.iris.Fissures.Time time) {
        try {
            if(getTime(serverName, serverDNS) == null) {
                MicroSecondDate ms = new MicroSecondDate(time);
                setTimeStmt.setString(1, serverName);
                setTimeStmt.setString(2, serverDNS);
                setTimeStmt.setTimestamp(3, ms.getTimestamp());
                setTimeStmt.executeUpdate();
            } else {
                updateTime(serverName, serverDNS, time);
            }
        } catch(SQLException sqle) {
            edu.sc.seis.sod.CommonAccess.handleException("Problem in setTime", sqle);
        }
    }


    //  public void setServerDNS(String serverDNS) {
    //  this.serverDNS = serverDNS;
    //     }

    //     public void setServerName(String serverName) {

    //  this.serverName = serverName;
    //     }


    public synchronized edu.iris.Fissures.Time getTime(String serverName,
                                                       String serverDNS) {
        try {
            getTimeStmt.setString(1, serverName);
            getTimeStmt.setString(2, serverDNS);
            ResultSet rs = getTimeStmt.executeQuery();
            if(rs.next()) {
                MicroSecondDate ms = new MicroSecondDate(rs.getTimestamp("time"));
                return ms.getFissuresTime();
            }
            return null;
        } catch(SQLException sqle) {
            edu.sc.seis.sod.CommonAccess.handleException("Problem in getTime", sqle);
            return null;
        }
    }

    public synchronized void incrementTime(String serverName,
                                           String serverDNS,
                                           int days) {
        edu.iris.Fissures.Time time = getTime(serverName, serverDNS);
        MicroSecondDate microSecondDate = new MicroSecondDate(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendar.setTime(microSecondDate);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        microSecondDate = new MicroSecondDate(calendar.getTime());
        time = microSecondDate.getFissuresTime();
        updateTime(serverName,
                   serverDNS,
                   time);
    }

    private void updateTime(String serverName,
                            String serverDNS,
                            edu.iris.Fissures.Time time) {
        try {
            MicroSecondDate ms = new MicroSecondDate(time);

            updateTimeStmt.setTimestamp(1, ms.getTimestamp());
            updateTimeStmt.setString(2, serverName);
            updateTimeStmt.setString(3, serverDNS);
            updateTimeStmt.executeUpdate();
        } catch(SQLException sqle) {
            edu.sc.seis.sod.CommonAccess.handleException("Problem in updateTime", sqle);
        }
    }


    private byte[] getBytes(Object obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            byte[] bytes = baos.toByteArray();
            baos.close();
            return bytes;
        } catch(Exception e) {
            edu.sc.seis.sod.CommonAccess.handleException("Problem in getBytes", e);
            return new byte[0];
        }
    }

    private Object getObject(byte[] bytes) {
        try {
            if(bytes == null) return null;
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object obj = ois.readObject();
            bais.close();
            ois.close();
            return obj;
        } catch(Exception e) {
            edu.sc.seis.sod.CommonAccess.handleException("Problem in getObject", e);
            return null;
        }
    }
    //this close method must be elimintaed the
    // database manager is responsible for closing
    // the connection and not the individual databases
    public void close() {
        try {
            connection.close();
        } catch(SQLException sqle) {
            edu.sc.seis.sod.CommonAccess.handleException("Problem in close", sqle);
        }

    }

    public void delete(String tableName) {
        try {
            connection.createStatement().execute(deleteStmt+tableName);
        } catch(SQLException sqle) {
            edu.sc.seis.sod.CommonAccess.handleException("Problem in delete", sqle);
        }
    }

    public void clean() {
        delete(this.tableName);
    }

    public String getTableName() {
        return this.tableName;
    }


    protected  Connection connection;

    protected String tableName;

    private PreparedStatement setTimeStmt;

    private PreparedStatement updateTimeStmt;

    private PreparedStatement getTimeStmt;

    private String deleteStmt;


}// AbstractConfigDatabase



package edu.sc.seis.sod.database;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.model.*;

import java.util.*;
import java.sql.*;


/**
 * WaveformDbQueue.java
 *
 *
 * Created: Mon Oct 14 12:07:06 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class WaveformDbQueue implements WaveformQueue{
    public WaveformDbQueue (Properties props){
	waveformDatabase = DatabaseManager.getDatabaseManager(props, "hsqldb").getWaveformDatabase();
	if(getPersistanceType(props) == 0) {
	    waveformDatabase.updateStatus(Status.PROCESSING, Status.NEW);
	    //restoreDb();
	    //delete(Status.COMPLETE_SUCCESS);
	} else {
	    // waveformDatabase(Status.COMPLETE_SUCCESS);
	    // waveformDatab(Status.PROCESSING);
	}
		
    }

  
    
    public void push(int waveformeventid, 
		     int siteid,
		     int waveformnetworkid) {
	Connection connection = getConnection();
	synchronized(connection) {
	    waveformDatabase.putChannelInfo(waveformeventid,
					    waveformnetworkid, 
					    siteid, 
					    new MicroSecondDate());
	    connection.notifyAll();
	}

    }

    public void clean() {
	if(Start.REMOVE_DATABASE == true) {
	    waveformDatabase.clean();
	}
    }

   
    public int pop() {
	
	Connection connection = getConnection();
	synchronized(connection) {
	    System.out.println("******************&&&&&&&&&&&*************************&*&&&&& WAITING IN POP");
	    
	    int dbid = waveformDatabase.getFirst();
	    System.out.println("******************&&&&&&&&&&&*************************&*&&&&& ENDED IN POP");
	     while(dbid == -1 && sourceAlive == true ) {
		try {
		    System.out.println("******************&&&&&&&&&&&*************************&*&&&&& WAITING IN POP one");
		    connection.wait();
		    System.out.println("******************&&&&&&&&&&&*************************&*&&&&& ENDED IN POP one");
		} catch(InterruptedException ie) {
		    ie.printStackTrace();
		}
		System.out.println(" BEFORE FIRST AGAIN ");
		dbid = waveformDatabase.getFirst();
		System.out.println(" AFTER FIRST AGAIN");
	    }
	    System.out.println("The Dbid that is returned is "+dbid);
	    
	    return dbid;
	}
    }


    public synchronized int getWaveformId(int waveformeventid, int waveformnetworkid) {
	return waveformDatabase.getChannelDbId(waveformeventid,
					       waveformnetworkid);
    }

    public synchronized void setStatus(int dbid, Status newStatus) {
	waveformDatabase.updateStatus(dbid, newStatus);
    }

    public synchronized void setStatus(int dbid, Status newStatus, String reason) {
	waveformDatabase.updateStatus(dbid, newStatus, reason);
    }

    public synchronized int getWaveformEventId(int dbid) {
	return waveformDatabase.getWaveformEventId(dbid);
    }
    
    public synchronized int getWaveformChannelId(int dbid) {
	return waveformDatabase.getWaveformChannelId(dbid);
    }


    /**
     * sets if the source i.e., the thread which pushes objects into the queue
     * is alive 
     *
     * @param value a <code>boolean</code> value
     */
    public synchronized void setSourceAlive(boolean value) {
	this.sourceAlive = value;
	notifyAll();
    }
    

//  /**
//      * returns the length of the queue.
//      *
//      * @return an <code>int</code> value
//      */
//     public synchronized int getLength() {
// 	int numNew = waveformDatabase.getCount(Status.NEW);
// 	int numProcessing = waveformDatabase.getCount(Status.PROCESSING);
// 	int numSuccessful = waveformDatabase.getCount(Status.COMPLETE_SUCCESS);
// 	System.out.println("THE NUMBER OF WAVEFORMEVENTCHANNELS THAT ARE NEW :::::::::::::::::::: "+numNew);
// 	System.out.println("THE NUMBER OF WAVEFORMEVENTCHANNELS THAT ARE PROCESSED :::::::::::::: "+numProcessing);
// 	System.out.println("THE NUMBER OF WAVEFORMEVENTCHANNELS THAT ARE SUCCESSFUL :::::::::::::::::: "+numSuccessful);
// 	return (numNew + numProcessing);
//     }
    
   
    public void delete(int waveformEventid) {
	waveformDatabase.delete(waveformEventid);
    }

    public synchronized int putInfo(int waveformeventid, int numNetworks) {
	return waveformDatabase.putInfo(waveformeventid, 
					numNetworks);
    }

    public synchronized int putNetworkInfo(int waveformeventid,
			      int networkid,
			      int numStations,
			      MicroSecondDate date) {
	return waveformDatabase.putNetworkInfo(waveformeventid,
				       networkid,
				       numStations,
				       date);
    }
	

    public synchronized int putStationInfo(int waveformeventid,
					   int stationid,
					   int networkid,
					   int numSites,
					   MicroSecondDate date) {

	return waveformDatabase.putStationInfo(waveformeventid,
					       stationid,
					       networkid,
					       numSites,
					       date);
    }

    public synchronized int putSiteInfo(int waveformeventid,
					int siteid,
					int stationid,
					int numChannels,
					MicroSecondDate date) {
	System.out.println("In waveform Queue in the method putSiteInfo");
	return waveformDatabase.putSiteInfo(waveformeventid,
					    siteid,
					    stationid,
					    numChannels,
					    date);
    }

   //  public synchronized int putChannelInfo(int waveformeventid,
// 					   int channelid,
// 					   int siteid,
// 					   MicroSecondDate date) {
// 	return waveformDatabase.putChannelInfo(waveformeventid,
// 					       channelid,
// 					       siteid,
// 					       date);
//     }

    public synchronized void decrementNetworkCount(int waveformeventid) {
	waveformDatabase.decrementNetworkCount(waveformeventid);
    }

    public synchronized void decrementStationCount(int waveformeventid, int networkid) {
	waveformDatabase.decrementStationCount(waveformeventid, networkid);
    }

    public synchronized void decrementSiteCount(int waveformeventid, int stationid) {
	waveformDatabase.decrementSiteCount(waveformeventid, stationid);
    }

    public synchronized void decrementChannelCount(int waveformeventid, int siteid) {
	waveformDatabase.decrementChannelCount(waveformeventid, siteid);
    }

    public void incrementNetworkCount(int waveformeventid) {
	waveformDatabase.incrementNetworkCount(waveformeventid);
    }

    public void incrementStationCount(int waveformeventid, int networkid) {
	waveformDatabase.incrementStationCount(waveformeventid, networkid);
    }

    public void incrementSiteCount(int waveformeventid, int stationid) {
	waveformDatabase.incrementSiteCount(waveformeventid, stationid);
    }

    public void incrementChannelCount(int waveformeventid, int siteid) {
	waveformDatabase.incrementChannelCount(waveformeventid, siteid);
    }

    public synchronized int getNetworkCount(int waveformeventid) {
	return waveformDatabase.getNetworkCount(waveformeventid);
    }

    public synchronized int getStationCount(int waveformeventid, int networkid) {
	return waveformDatabase.getStationCount(waveformeventid, networkid);
    }

    public synchronized int getSiteCount(int waveformeventid, int stationid) {
	return waveformDatabase.getSiteCount(waveformeventid, stationid);
    }

    public synchronized int getChannelCount(int waveformeventid, int siteid) {
	return waveformDatabase.getChannelCount(waveformeventid, siteid);
    }
    
    public int unfinishedNetworkCount(int waveformeventid) {
	return waveformDatabase.unfinishedNetworkCount(waveformeventid);
    }
    
    public int unfinishedStationCount(int waveformeventid, int networkid) {
	return waveformDatabase.unfinishedStationCount(waveformeventid, networkid);
    }

    public int unfinishedSiteCount(int waveformeventid, int stationid) {
	return waveformDatabase.unfinishedSiteCount(waveformeventid, stationid);
    }

    public int unfinishedChannelCount(int waveformeventid, int siteid) {
	return waveformDatabase.unfinishedChannelCount(waveformeventid, siteid);
    }

    public void deleteInfo(int waveformeventid) {
	waveformDatabase.deleteInfo(waveformeventid);
    }

    public void deleteNetworkInfo(int waveformeventid, int networkid) { 
	waveformDatabase.deleteNetworkInfo(waveformeventid, networkid);
    }

    public void deleteStationInfo(int waveformeventid, int stationid) {
	waveformDatabase.deleteStationInfo(waveformeventid, stationid);
    }

    public void deleteSiteInfo(int waveformeventid, int siteid) {
	waveformDatabase.deleteSiteInfo(waveformeventid, siteid);
    }

    public void deleteChannelInfo(int waveformeventid, int channelid) {
	waveformDatabase.deleteChannelInfo(waveformeventid, channelid);
    }

    public synchronized int[] getIds() {
	return waveformDatabase.getIds();
    }


    public synchronized void beginTransaction() {
	waveformDatabase.beginTransaction();
    }

    public synchronized void endTransaction() {
	waveformDatabase.endTransaction();
    }

    public Connection getConnection() {
	return waveformDatabase.getConnection();
    }

     private int getPersistanceType(Properties props) {
	 
	String value = props.getProperty("edu.sc.seis.sod.persistencetype");
	if(value == null) value = "ATLEASTONCE";
	if(value.equalsIgnoreCase("ATMOSTONCE")) return 1;
	else return 0;
     }

    
    
    private WaveformDatabase waveformDatabase;    

    private boolean sourceAlive = true;
    
}// WaveformDbQueue

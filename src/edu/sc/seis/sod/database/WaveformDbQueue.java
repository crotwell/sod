package edu.sc.seis.sod.database;

import edu.iris.Fissures.model.*;

import java.util.*;

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

  
    
    public synchronized void push(int waveformeventid, int waveformnetworkid) {
	waveformDatabase.putChannelInfo(waveformeventid,
			     waveformnetworkid, new MicroSecondDate());
	notifyAll();

    }

    public synchronized int pop() {
	
	int dbid = waveformDatabase.getFirst();
	
	while(dbid == -1 && sourceAlive == true ) {
	    try {
		wait();
	    } catch(InterruptedException ie) {
		ie.printStackTrace();
	    }
	    dbid = waveformDatabase.getFirst();
	}
	return dbid;
    }

    public int getWaveformId(int waveformeventid, int waveformnetworkid) {
	return waveformDatabase.getChannelDbId(waveformeventid,
					       waveformnetworkid);
    }

    public void setStatus(int dbid, Status newStatus) {
	waveformDatabase.updateStatus(dbid, newStatus);
    }

    public void setStatus(int dbid, Status newStatus, String reason) {
	waveformDatabase.updateStatus(dbid, newStatus, reason);
    }

    public int getWaveformEventId(int dbid) {
	return waveformDatabase.getWaveformEventId(dbid);
    }
    
    public int getWaveformChannelId(int dbid) {
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

    public int putInfo(int waveformeventid, int numNetworks) {
	return waveformDatabase.putInfo(waveformeventid, 
					numNetworks);
    }

    public int putNetworkInfo(int waveformeventid,
			      int networkid,
			      int numStations,
			      MicroSecondDate date) {
	return waveformDatabase.putNetworkInfo(waveformeventid,
				       networkid,
				       numStations,
				       date);
    }
	
    public int putStationInfo(int waveformeventid,
			      int stationid,
			      int numSites,
			      MicroSecondDate date) {

	return waveformDatabase.putStationInfo(waveformeventid,
					stationid,
					numSites,
					date);
    }

    public int putSiteInfo(int waveformeventid,
			   int siteid,
			   int numChannels,
			   MicroSecondDate date) {
	return waveformDatabase.putSiteInfo(waveformeventid,
				     siteid,
				     numChannels,
				     date);
    }

    public int putChannelInfo(int waveformeventid,
			      int channelid,
			      MicroSecondDate date) {
	return waveformDatabase.putChannelInfo(waveformeventid,
					channelid,
					date);
    }

    public void decrementNetworkCount(int waveformeventid) {
	waveformDatabase.decrementNetworkCount(waveformeventid);
    }

    public void decrementStationCount(int waveformeventid, int networkid) {
	waveformDatabase.decrementStationCount(waveformeventid, networkid);
    }

    public void decrementSiteCount(int waveformeventid, int stationid) {
	waveformDatabase.decrementSiteCount(waveformeventid, stationid);
    }

    public void decrementChannelCount(int waveformeventid, int siteid) {
	waveformDatabase.decrementChannelCount(waveformeventid, siteid);
    }

    public int getNetworkCount(int waveformeventid) {
	return waveformDatabase.getNetworkCount(waveformeventid);
    }

    public int getStationCount(int waveformeventid, int networkid) {
	return waveformDatabase.getStationCount(waveformeventid, networkid);
    }

    public int getSiteCount(int waveformeventid, int stationid) {
	return waveformDatabase.getSiteCount(waveformeventid, stationid);
    }

    public int getChannelCount(int waveformeventid, int siteid) {
	return waveformDatabase.getChannelCount(waveformeventid, siteid);
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

    public int[] getIds() {
	return waveformDatabase.getIds();
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

package edu.sc.seis.sod.database;


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
	    //delete(Status.COMPLETE_SUCCESS);
	} else {
	    // waveformDatabase(Status.COMPLETE_SUCCESS);
	    // waveformDatab(Status.PROCESSING);
	}
    }
    
    public synchronized void push(int waveformeventid, int waveformnetworkid) {
	waveformDatabase.put(waveformeventid,
			     waveformnetworkid);
	notifyAll();

    }

    public synchronized int pop() {
	
	int dbid = waveformDatabase.getFirst();
	
	while(dbid == -1 && sourceAlive == true) {
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
	return waveformDatabase.getId(waveformeventid,
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
    
    public int getWaveformNetworkId(int dbid) {
	return waveformDatabase.getWaveformNetworkId(dbid);
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
    

 /**
     * returns the length of the queue.
     *
     * @return an <code>int</code> value
     */
    public synchronized int getLength() {
	int numNew = waveformDatabase.getCount(Status.NEW);
	int numProcessing = waveformDatabase.getCount(Status.PROCESSING);
	int numSuccessful = waveformDatabase.getCount(Status.COMPLETE_SUCCESS);
	System.out.println("THE NUMBER OF WAVEFORMEVENTCHANNELS THAT ARE NEW :::::::::::::::::::: "+numNew);
	System.out.println("THE NUMBER OF WAVEFORMEVENTCHANNELS THAT ARE PROCESSED :::::::::::::: "+numProcessing);
	System.out.println("THE NUMBER OF WAVEFORMEVENTCHANNELS THAT ARE SUCCESSFUL :::::::::::::::::: "+numSuccessful);
	return (numNew + numProcessing);
    }
    
    public int getSuccessfulChannelCount(int waveformEventid) {
	return waveformDatabase.getSuccessfulChannelCount(waveformEventid);
    }


    public void delete(int waveformEventid) {
	waveformDatabase.delete(waveformEventid);
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

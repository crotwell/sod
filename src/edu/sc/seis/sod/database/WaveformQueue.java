package edu.sc.seis.sod.database;

import edu.iris.Fissures.model.*;

import java.sql.*;

/**
 * WaveformQueue.java
 *
 *
 * Created: Mon Oct 14 11:50:06 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public interface WaveformQueue {
    
    public void push(int waveformeventid, int siteid, int waveformnetworkid);

    public long pop();
   
    public long getWaveformId(int waveformeventid, int waveformnetworkid);

    public void setStatus(long dbid, Status newStatus);

    public void setStatus(long dbid, Status newStatus, String reason);

    public int getWaveformEventId(long dbid);

    public int getWaveformChannelId(long dbid);

    // public int getLength();
    
  
    public void delete(int waveformEventid);
    
    public void setSourceAlive(boolean bool);

 public int putInfo(int waveformeventid, int numNetworks);

    public int putNetworkInfo(int waveformeventid,
			  int networkid,
			  int numStations,
			  MicroSecondDate date);

    public int putStationInfo(int waveformeventid,
			      int stationid,
			      int networkid,
			      int numSites,
			      MicroSecondDate date);

    public int putSiteInfo(int waveformeventid,
			   int siteid,
			   int stationid, 
			   int numChannels,
			   MicroSecondDate date);

//     public int putChannelInfo(int waveformeventid,
// 			      int channelid,
// 			      int siteid,
// 			      MicroSecondDate date);

    public void decrementNetworkCount(int waveformeventid);

    public void decrementStationCount(int waveformeventid, int networkid);

    public void decrementSiteCount(int waveformeventid, int stationid);

    public void decrementChannelCount(int waveformeventid, int siteid);

    public void incrementNetworkCount(int waveformeventid);

    public void incrementStationCount(int waveformeventid, int networkid);

    public void incrementSiteCount(int waveformeventid, int stationid);

    public void incrementChannelCount(int waveformeventid, int siteid);

    public int getNetworkCount(int waveformreventid);

    public int getStationCount(int waveformeventid, int networkid);

    public int getSiteCount(int waveformeventid, int stationid);

    public int getChannelCount(int waveformeventid, int siteid);

    public int unfinishedNetworkCount(int waveformeventid);

    public int unfinishedStationCount(int waveformeventid, int networkid);

    public int unfinishedSiteCount(int waveformeventid, int stationid);

    public int unfinishedChannelCount(int waveformeventid, int siteid);

    public void deleteInfo(int waveformeventid);

    public void deleteNetworkInfo(int waveformeventid, int networkid);

    public void deleteStationInfo(int waveformeventid, int stationid);

    public void deleteSiteInfo(int waveformeventid, int siteid);

    public void deleteChannelInfo(int waveformeventid, int channelid);

    public long[] getIds();

    public void clean();

    public void beginTransaction();

    public void endTransaction();

    public Object getConnection();

}// WaveformQueue

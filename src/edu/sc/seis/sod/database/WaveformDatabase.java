package edu.sc.seis.sod.database;

import edu.iris.Fissures.model.*;

/**
 * WaveformDatabase.java
 *
 *
 * Created: Fri Oct 11 14:39:43 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public interface WaveformDatabase {
    //define the methods.
    public int putInfo(int waveformeventid, int numNetworks);

    public int putNetworkInfo(int waveformeventid,
			  int networkid,
			  int numStations,
			  MicroSecondDate date);

    public int putStationInfo(int waveformeventid,
			      int stationid,
			      int numSites,
			      MicroSecondDate date);

    public int putSiteInfo(int waveformeventid,
			   int siteid,
			   int numChannels,
			   MicroSecondDate date);

    public int putChannelInfo(int waveformeventid,
			      int channelid,
			      MicroSecondDate date);

    public void decrementNetworkCount(int waveformeventid);

    public void decrementStationCount(int waveformeventid, int networkid);

    public void decrementSiteCount(int waveformeventid, int stationid);

    public void decrementChannelCount(int waveformeventid, int siteid);

    public int getNetworkCount(int waveformreventid);

    public int getStationCount(int waveformeventid, int networkid);

    public int getSiteCount(int waveformeventid, int stationid);

    public int getChannelCount(int waveformeventid, int siteid);

    public int getChannelDbId(int waveformeventid, int waveformchannelid);

    public int getFirst();

    public void updateStatus(int waveformid, Status newStatus);

    public void updateStatus(int waveformid, Status newStatus, String reason);

    public void updateStatus(Status status, Status newStatus);

    public int[] getByStatus(Status status);

    public int getWaveformEventId(int dbid);

    public int getWaveformChannelId(int dbid);

    public void delete(int waveformEventid);
    
    public void deleteInfo(int waveformeventid);

    public void deleteNetworkInfo(int waveformeventid, int networkid);

    public void deleteStationInfo(int waveformeventid, int stationid);

    public void deleteSiteInfo(int waveformeventid, int siteid);

    public void deleteChannelInfo(int waveformeventid, int channelid);

    public int[] getIds();

    public void clean();
}// WaveformDatabase

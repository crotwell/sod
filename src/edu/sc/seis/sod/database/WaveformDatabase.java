package edu.sc.seis.sod.database;

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
    public int getId(int waveformeventid, int waveformnetworkid);

    public int put(int waveformeventid, int waveformnetworkid);
    
    public int getFirst();

    public void updateStatus(int waveformid, Status newStatus);

    public void updateStatus(int waveformid, Status newStatus, String reason);

    public void updateStatus(Status status, Status newStatus);

    public int[] getByStatus(Status status);

    public int getWaveformEventId(int dbid);

    public int getWaveformNetworkId(int dbid);

    public int getSuccessfulChannelCount(int waveformEventid);
    
    public void delete(int waveformEventid);
    
    
    public int getCount(Status status);
    
}// WaveformDatabase

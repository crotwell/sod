package edu.sc.seis.sod.database;

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
    
    public void push(int waveformeventid, int waveformnetworkid);

    public int pop();
   
    public int getWaveformId(int waveformeventid, int waveformnetworkid);

    public void setStatus(int dbid, Status newStatus);

    public void setStatus(int dbid, Status newStatus, String reason);

    public int getWaveformEventId(int dbid);

    public int getWaveformNetworkId(int dbid);

    public int getLength();
    
    public int getSuccessfulChannelCount(int waveformEventid);

    public void delete(int waveformEventid);
    
    public void setSourceAlive(boolean bool);
}// WaveformQueue

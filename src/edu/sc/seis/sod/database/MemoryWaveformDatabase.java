package edu.sc.seis.sod.database;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.model.*;

import java.util.*;
import java.sql.*;

/**
 * MemoryWaveformDatabase.java
 *
 *
 * Created: Mon Mar 24 12:35:24 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class MemoryWaveformDatabase extends AbstractWaveformDatabase{
    public MemoryWaveformDatabase (){
        super(null);
    }

    public void create() {
        waveformMap = new HashMap();
        networkMap = new HashMap();
        stationMap = new HashMap();
        siteMap = new HashMap();
        channelMap = new TreeMap();
    }

    public synchronized int putInfo(int waveformeventid, int numNetworks) {
        Long key = new Long((((long)waveformeventid)<<32) + waveformeventid);
        waveformMap.put(key, new Wrapper(0,numNetworks,waveformeventid, waveformeventid));
        return 0;
    }

    public synchronized int putNetworkInfo(int waveformeventid,
                              int networkid,
                              int numStations,
                              MicroSecondDate date) {

        Long key = new Long((((long)waveformeventid)<<32) + networkid);
        networkMap.put(key, new Wrapper(0, numStations, waveformeventid, networkid));
        return 0;
    }

    public synchronized int putStationInfo(int waveformeventid,
                              int stationid,
                              int networkid,
                              int numSites,
                              MicroSecondDate date) {
        Long key = new Long((((long)waveformeventid)<<32) + stationid);
        stationMap.put(key, new Wrapper(networkid, numSites, waveformeventid, stationid));
        return 0;
    }
        

    public synchronized int putSiteInfo(int waveformeventid,
                           int siteid,
                           int stationid,
                           int numChannels,
                           MicroSecondDate date) {
        Long key = new Long((((long)waveformeventid)<<32) + siteid);
        siteMap.put(key, new Wrapper(stationid, numChannels, waveformeventid, siteid));
        return 0;
    }

    public synchronized long putChannelInfo(int waveformeventid,
                              int channelid,
                              int siteid,
                              MicroSecondDate date) {
        Long key = new Long((((long)waveformeventid)<<32) + channelid);
        channelMap.put(key, new Wrapper(siteid, 0, waveformeventid, channelid));
        return key.longValue();
    }
    
    public synchronized void decrementNetworkCount(int waveformeventid) {
        Long key = new Long((((long)waveformeventid)<<32) + waveformeventid);
        int count = getNetworkCount(waveformeventid) - 1;
        Wrapper obj = (Wrapper) waveformMap.get(key);
        obj.count = count;
    }

    public synchronized void decrementStationCount(int waveformeventid, int networkid) {
        Long key = new Long((((long)waveformeventid)<<32) + networkid);
        int count = getStationCount(waveformeventid, networkid) - 1;
        Wrapper obj = (Wrapper) networkMap.get(key);
        obj.count = count;
    }

    public synchronized void decrementSiteCount(int waveformeventid, int stationid) {
        Long key = new Long((((long)waveformeventid)<<32) + stationid);
        int count = getSiteCount(waveformeventid, stationid) - 1;
        Wrapper obj = (Wrapper) stationMap.get(key);
        obj.count = count;
    }

    public synchronized void decrementChannelCount(int waveformeventid, int siteid) {
        Long key = new Long((((long)waveformeventid)<<32) + siteid);
        int count = getChannelCount(waveformeventid, siteid) - 1;
        Wrapper obj = (Wrapper) siteMap.get(key);
        obj.count = count;
    }

    public synchronized void incrementNetworkCount(int waveformeventid) {
        int count = getNetworkCount(waveformeventid) + 1;
        Long key = new Long((((long)waveformeventid)<<32) + waveformeventid);
        Wrapper obj = (Wrapper) waveformMap.get(key);
        obj.count = count;
    }

    public synchronized void incrementStationCount(int waveformeventid, int networkid) {
        int count = getStationCount(waveformeventid, networkid) + 1;
        Long key = new Long((((long)waveformeventid)<<32) + networkid);
        Wrapper obj = (Wrapper) networkMap.get(key);
        obj.count = count;
    }

    public synchronized void incrementSiteCount(int waveformeventid, int stationid) {
        int count = getSiteCount(waveformeventid, stationid) + 1;
        Long key = new Long((((long)waveformeventid)<<32) + stationid);
        Wrapper obj = (Wrapper) stationMap.get(key);
        obj.count = count;
    }

    public synchronized void incrementChannelCount(int waveformeventid, int siteid) {
        int count = getChannelCount(waveformeventid, siteid) + 1;
        Long key = new Long((((long)waveformeventid)<<32) + siteid);
        Wrapper obj = (Wrapper) siteMap.get(key);
        obj.count = count;
    }

    public synchronized int getNetworkCount(int waveformeventid) {
        Long key = new Long((((long)waveformeventid)<<32) + waveformeventid);
        Wrapper obj = (Wrapper) waveformMap.get(key);
        return obj.count;
    }

    public synchronized int getStationCount(int waveformeventid, int networkid) {
        Long key = new Long((((long)waveformeventid)<<32) + networkid);
        Wrapper obj = (Wrapper) networkMap.get(key);
        return obj.count;
        
    }

    public synchronized int getSiteCount(int waveformeventid, int stationid) {
        Long key = new Long((((long)waveformeventid)<<32) + stationid);
        Wrapper obj = (Wrapper) stationMap.get(key);
        return obj.count;
    }

    public synchronized int getChannelCount(int waveformeventid, int siteid) {
        Long key = new Long((((long)waveformeventid)<<32) + siteid);
        Wrapper obj = (Wrapper) siteMap.get(key);
        return obj.count;
    }

    public synchronized int unfinishedNetworkCount(int waveformeventid) {
        return getNetworkCount(waveformeventid);
    }

    public synchronized int unfinishedStationCount(int waveformeventid, int networkid) {
        return getStationCount(waveformeventid, networkid);
    }

    public synchronized int unfinishedSiteCount(int waveformeventid, int stationid) {
        return getSiteCount(waveformeventid, stationid);
    }

    public synchronized int unfinishedChannelCount(int waveformeventid, int siteid) {
        return getChannelCount(waveformeventid, siteid);
    }

    public synchronized long getChannelDbId(int waveformeventid, int waveformchannelid) {

        Set keySet = channelMap.keySet();
        Iterator iterator = keySet.iterator();
        while(iterator.hasNext()) {
            Long key = (Long) iterator.next();
            Wrapper obj = (Wrapper) channelMap.get(key);
            if(obj.waveformeventid == waveformeventid &&
               obj.waveformoriginalid == waveformchannelid) {
                return key.longValue();
            }
        }
        return -1;
    }

    public synchronized long getFirst() {
        Set keySet = channelMap.keySet();
        Iterator iterator = keySet.iterator();
        while(iterator.hasNext()) {
            Long key = (Long) iterator.next();
            Wrapper obj = (Wrapper) channelMap.get(key);
            if(obj.status == Status.NEW.getId()) {
                obj.status = Status.PROCESSING.getId();
                return key.longValue();
            }
        }
        return -1;
    }

    public synchronized void updateStatus(long waveformid, Status newStatus) {
        Long key = new Long(waveformid);
        Wrapper obj = (Wrapper) channelMap.get(key);
        obj.status = newStatus.getId();
    }

    public synchronized void updateStatus(long waveformid, Status newStatus, String reason) {
        updateStatus(waveformid, newStatus);
    }

    public synchronized void updateStatus(Status status, Status newStatus) {
        Collection values = channelMap.values();
        Iterator iterator = values.iterator();
        while(iterator.hasNext()) {
            Wrapper obj = (Wrapper) iterator.next();
            if(obj.status == status.getId()) {
                obj.status = newStatus.getId();
            }
        }
    }

    public synchronized long[] getByStatus(Status status) {
        ArrayList arrayList = new ArrayList();
        Set keySet = channelMap.keySet();
        Iterator iterator = keySet.iterator();
        while(iterator.hasNext()) {
            Long key = (Long) iterator.next();
            Wrapper obj = (Wrapper) channelMap.get(key);
            if(obj.status == status.getId()) {
                arrayList.add(key);
            }
        }
        long[] rtnValues = new long[arrayList.size()];

        for(int counter = 0; counter < arrayList.size(); counter++) {
            rtnValues[counter] = ((Long)arrayList.get(counter)).longValue();
        }
        return rtnValues;
    }

    public synchronized int getWaveformEventId(long dbid) {
        Long key = new Long(dbid);
        Wrapper wrapper = (Wrapper) channelMap.get(key);
        return wrapper.waveformeventid;
    }

    public synchronized int getWaveformChannelId(long dbid) {
        Long key = new Long(dbid);
        Wrapper wrapper = (Wrapper) channelMap.get(key);
        return wrapper.waveformoriginalid;
    }

    public synchronized void delete(int waveformEventid) {

    }
    
    public synchronized void deleteInfo(int waveformeventid) {

    }

    public synchronized void deleteNetworkInfo(int waveformeventid, int networkid) {

    }

    public synchronized void deleteStationInfo(int waveformeventid, int stationid) {

    }

    public synchronized void deleteSiteInfo(int waveformeventid, int siteid) {

    }

    public synchronized void deleteChannelInfo(int waveformeventid, int channelid) {

    }

    public synchronized long[] getIds() {
        ArrayList arrayList = new ArrayList();
        Set keySet = channelMap.keySet();
        Iterator iterator = keySet.iterator();
        while(iterator.hasNext()) {
            Long key = (Long) iterator.next();
            arrayList.add(key);
        }
        long[] rtnValues = new long[arrayList.size()];

        for(int counter = 0; counter < arrayList.size(); counter++) {
            rtnValues[counter] = ((Long)arrayList.get(counter)).longValue();
        }
        return rtnValues;
    }

    public synchronized void clean() {

    }

    public synchronized void beginTransaction() {

    }

    public synchronized void endTransaction() {

    }

    public synchronized Object getConnection() {
        return CommonAccess.getCommonAccess();
    }

    private class Wrapper {
        public Wrapper(int refid, int count, int waveformeventid, int waveformoriginalid) {
            this.refid = refid;
            this.count = count;
            this.status = 0;
            this.waveformeventid = waveformeventid;
            this.waveformoriginalid = waveformoriginalid;
        }
        
        public int refid;
        
        public int count;

        public int status;

        public int waveformeventid;
        
        public int waveformoriginalid;
    }

    private HashMap waveformMap;
    
    private HashMap networkMap;
    
    private HashMap stationMap;

    private HashMap siteMap;
    
    private Map channelMap;
    
  
}// MemoryWaveformDatabase

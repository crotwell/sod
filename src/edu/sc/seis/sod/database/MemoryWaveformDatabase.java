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
        channelMap = new HashMap();
    }

    public int putInfo(int waveformeventid, int numNetworks) {
        String key = Integer.toString(waveformeventid);
        waveformMap.put(key, new Wrapper(0,numNetworks,waveformeventid, waveformeventid));
        return Integer.parseInt(key);
    }

    public int putNetworkInfo(int waveformeventid,
                              int networkid,
                              int numStations,
                              MicroSecondDate date) {

        String key = Integer.toString(waveformeventid+networkid);
        networkMap.put(key, new Wrapper(0, numStations, waveformeventid, networkid));
        return Integer.parseInt(key);
    }

    public int putStationInfo(int waveformeventid,
                              int stationid,
                              int networkid,
                              int numSites,
                              MicroSecondDate date) {
        String key = Integer.toString(waveformeventid + stationid);
        stationMap.put(key, new Wrapper(networkid, numSites, waveformeventid, stationid));
        return Integer.parseInt(key);
    }
        

    public int putSiteInfo(int waveformeventid,
                           int siteid,
                           int stationid,
                           int numChannels,
                           MicroSecondDate date) {

        String key = Integer.toString(waveformeventid + siteid);
        siteMap.put(key, new Wrapper(stationid, numChannels, waveformeventid, siteid));
        return Integer.parseInt(key);
    }

    public int putChannelInfo(int waveformeventid,
                              int channelid,
                              int siteid,
                              MicroSecondDate date) {
        String key = Integer.toString(waveformeventid + channelid);
        channelMap.put(key, new Wrapper(siteid, 0, waveformeventid, channelid));
        return Integer.parseInt(key);
    }
    
    public void decrementNetworkCount(int waveformeventid) {
        int count = getNetworkCount(waveformeventid) - 1;
        String key = Integer.toString(waveformeventid);
        Wrapper obj = (Wrapper) waveformMap.get(key);
        obj.count = count;
    }

    public void decrementStationCount(int waveformeventid, int networkid) {
        int count = getStationCount(waveformeventid, networkid) - 1;
        String key = Integer.toString(waveformeventid + networkid);
        Wrapper obj = (Wrapper) networkMap.get(key);
        obj.count = count;
    }

    public void decrementSiteCount(int waveformeventid, int stationid) {
        int count = getSiteCount(waveformeventid, stationid) - 1;
        String key = Integer.toString(waveformeventid + stationid);
        Wrapper obj = (Wrapper) stationMap.get(key);
        obj.count = count;
    }

    public void decrementChannelCount(int waveformeventid, int siteid) {
        int count = getChannelCount(waveformeventid, siteid) - 1;
        String key = Integer.toString(waveformeventid + siteid);
        Wrapper obj = (Wrapper) siteMap.get(key);
        obj.count = count;
    }

    public void incrementNetworkCount(int waveformeventid) {
        int count = getNetworkCount(waveformeventid) + 1;
        String key = Integer.toString(waveformeventid);
        Wrapper obj = (Wrapper) waveformMap.get(key);
        obj.count = count;
    }

    public void incrementStationCount(int waveformeventid, int networkid) {
        int count = getStationCount(waveformeventid, networkid) + 1;
        String key = Integer.toString(waveformeventid + networkid);
        Wrapper obj = (Wrapper) networkMap.get(key);
        obj.count = count;
    }

    public void incrementSiteCount(int waveformeventid, int stationid) {
        int count = getSiteCount(waveformeventid, stationid) + 1;
        String key = Integer.toString(waveformeventid + stationid);
        Wrapper obj = (Wrapper) stationMap.get(key);
        obj.count = count;
    }

    public void incrementChannelCount(int waveformeventid, int siteid) {
        int count = getChannelCount(waveformeventid, siteid) + 1;
        String key =  Integer.toString(waveformeventid + siteid);
        Wrapper obj = (Wrapper) siteMap.get(key);
        obj.count = count;
    }

    public int getNetworkCount(int waveformeventid) {
        String key = Integer.toString(waveformeventid);
        Wrapper obj = (Wrapper) waveformMap.get(key);
        return obj.count;
    }

    public int getStationCount(int waveformeventid, int networkid) {
        String key = Integer.toString(waveformeventid + networkid);
        Wrapper obj = (Wrapper) networkMap.get(key);
        return obj.count;
        
    }

    public int getSiteCount(int waveformeventid, int stationid) {
        String key = Integer.toString(waveformeventid + stationid);
        Wrapper obj = (Wrapper) stationMap.get(key);
        return obj.count;
    }

    public int getChannelCount(int waveformeventid, int siteid) {
        String key = Integer.toString(waveformeventid + siteid);
        Wrapper obj = (Wrapper) siteMap.get(key);
        return obj.count;
    }

    public int unfinishedNetworkCount(int waveformeventid) {
        return getNetworkCount(waveformeventid);
    }

    public int unfinishedStationCount(int waveformeventid, int networkid) {
        return getStationCount(waveformeventid, networkid);
    }

    public int unfinishedSiteCount(int waveformeventid, int stationid) {
        return getSiteCount(waveformeventid, stationid);
    }

    public int unfinishedChannelCount(int waveformeventid, int siteid) {
        return getChannelCount(waveformeventid, siteid);
    }

    public int getChannelDbId(int waveformeventid, int waveformchannelid) {
        return (waveformeventid + waveformchannelid);
    }

    public int getFirst() {
        Set keySet = channelMap.keySet();
        Iterator iterator = keySet.iterator();
        while(iterator.hasNext()) {
            String key = (String) iterator.next();
            Wrapper obj = (Wrapper) channelMap.get(key);
            if(obj.status == Status.NEW.getId()) {
                obj.status = Status.PROCESSING.getId();
                return Integer.parseInt(key);
            }
        }
        return -1;
    }

    public void updateStatus(int waveformid, Status newStatus) {
        String key = Integer.toString(waveformid);
        Wrapper obj = (Wrapper) channelMap.get(key);
        obj.status = newStatus.getId();
    }

    public void updateStatus(int waveformid, Status newStatus, String reason) {
        updateStatus(waveformid, newStatus);
    }

    public void updateStatus(Status status, Status newStatus) {
        Collection values = channelMap.values();
        Iterator iterator = values.iterator();
        while(iterator.hasNext()) {
            Wrapper obj = (Wrapper) iterator.next();
            if(obj.status == status.getId()) {
                obj.status = newStatus.getId();
            }
        }
    }

    public int[] getByStatus(Status status) {
        ArrayList arrayList = new ArrayList();
        Set keySet = channelMap.keySet();
        Iterator iterator = keySet.iterator();
        while(iterator.hasNext()) {
            String key = (String) iterator.next();
            Wrapper obj = (Wrapper) channelMap.get(key);
            if(obj.status == status.getId()) {
                arrayList.add(new Integer(Integer.parseInt(key)));
            }
        }
        int[] rtnValues = new int[arrayList.size()];

        for(int counter = 0; counter < arrayList.size(); counter++) {
            rtnValues[counter] = ((Integer)arrayList.get(counter)).intValue();
        }
        return rtnValues;
    }

    public int getWaveformEventId(int dbid) {
        String key = Integer.toString(dbid);
        Wrapper wrapper = (Wrapper) channelMap.get(key);
        return wrapper.waveformeventid;
    }

    public int getWaveformChannelId(int dbid) {
        String key = Integer.toString(dbid);
        Wrapper wrapper = (Wrapper) channelMap.get(key);
        return wrapper.waveformoriginalid;
    }

    public void delete(int waveformEventid) {

    }
    
    public void deleteInfo(int waveformeventid) {

    }

    public void deleteNetworkInfo(int waveformeventid, int networkid) {

    }

    public void deleteStationInfo(int waveformeventid, int stationid) {

    }

    public void deleteSiteInfo(int waveformeventid, int siteid) {

    }

    public void deleteChannelInfo(int waveformeventid, int channelid) {

    }

    public int[] getIds() {
        ArrayList arrayList = new ArrayList();
        Set keySet = channelMap.keySet();
        Iterator iterator = keySet.iterator();
        while(iterator.hasNext()) {
            String key = (String) iterator.next();
            arrayList.add(new Integer(Integer.parseInt(key)));
        }
        int[] rtnValues = new int[arrayList.size()];

        for(int counter = 0; counter < arrayList.size(); counter++) {
            rtnValues[counter] = ((Integer)arrayList.get(counter)).intValue();
        }
        return rtnValues;
    }

    public void clean() {

    }

    public void beginTransaction() {

    }

    public void endTransaction() {

    }

    public Object getConnection() {
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
    
    private HashMap channelMap;
    
  
}// MemoryWaveformDatabase

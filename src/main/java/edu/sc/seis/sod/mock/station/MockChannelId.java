package edu.sc.seis.sod.mock.station;

import java.time.Instant;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.model.station.ChannelId;

public class MockChannelId{
    public static ChannelId createVerticalChanId(){  return createChanId("BHZ", "00", MockStation.createStation()); }

    public static ChannelId createNorthChanId(){ return createChanId("BHN", "00", MockStation.createStation()); }
    
    public static ChannelId createEastChanId(){ return createChanId("BHE", "00", MockStation.createStation()); }

    public static ChannelId createOtherNetChanId(){
        return createChanId("BHZ", "00", MockStation.createOtherStation());
    }
    
    public static ChannelId createOtherSiteSameStationChanId(){
        return createChanId("BHZ", "01", MockStation.createStation());
    }

    public static ChannelId createChanId(String chanCode, String locCode, Station station){
        ChannelId chanId = new ChannelId(station.getNetworkId(),
        		station.getCode(),
        		locCode,
        		chanCode,
        		station.getStartDateTime());
        return chanId;
    }

    public static ChannelId makeChanId(Instant time) {
        ChannelId chanId =  createVerticalChanId();
        chanId.setStartTime(time);
        return chanId;
    }
}

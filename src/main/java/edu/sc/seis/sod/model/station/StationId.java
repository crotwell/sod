
package edu.sc.seis.sod.model.station;

import java.time.Instant;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;

/** Identifies a station. The additional begin_time is needed as station
 *  codes can be reused if, for example the station moves only a short 
 *  distance. The begin_time should be equal to the beginning
 *  effective time of the station, which is also in the Station object. 
 **/

public class StationId  {
    public StationId() {
    }

    public static StationId of(Station station) {
        return new StationId(station.getNetworkId(), station.getStationCode(), station.getStartDateTime());
    }
    
    @Deprecated
    public StationId(Station station) {
        this(station.getNetworkId(), station.getStationCode(), station.getStartDateTime());
    }
    

    
    public
    StationId(String network_id,
              String station_code,
              Instant startTime)
    {
        this.setNetworkId(network_id);
        this.setStationCode(station_code);
        this.startTime = startTime;
    }
    
    @Deprecated
    public
    StationId(NetworkId network_id,
              String station_code,
              Instant startTime)
    {
        this.setNetworkId(network_id.toString());
        this.setStationCode(station_code);
        this.startTime = startTime;
    }

    
    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }


    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }


    public Instant getStartTime() {
        return startTime;
    }


    private String networkId;
    private String stationCode;
    
    private Instant startTime;
}

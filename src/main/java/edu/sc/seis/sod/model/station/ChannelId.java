
package edu.sc.seis.sod.model.station;

import java.time.Instant;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;

/** Identifies a Channel. The additional startTime is needed 
 *  as channel
 *  codes are frequently reused if, for example a new sensor is install
 *  in place of the old one, which properly creates a new Channel.
 *  The startTime should be equal to the beginning
 *  effective time of the channel, which is also in the Channel object. 
 **/

public class ChannelId {
    
    ChannelId() {
    }

    public static ChannelId of(Channel chan) {
        return new ChannelId(chan.getNetwork().getNetworkId(), chan.getStationCode(), chan.getLocCode(), chan.getChannelCode(), chan.getStartDateTime());
    }
    
    @Deprecated
    public ChannelId(Channel chan) {
        this(chan.getNetwork().getNetworkId(), chan.getStationCode(), chan.getLocCode(), chan.getChannelCode(), chan.getStartDateTime());
    }
    
    public ChannelId(Station station, String locationCode, String channelCode, Instant startTime) {
        this(station.getNetworkId(),
             station.getStationCode(),
             locationCode,
             channelCode,
             startTime);
    }

    public
    ChannelId(String networkId,
              String stationCode,
              String locCode,
              String channelCode,
              Instant startTime)
    {
        this.setNetworkId(networkId);
        this.setStationCode(stationCode);
        this.setLocCode(locCode);
        this.setChannelCode(channelCode);
        this.setStartTime(startTime);
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }
    
    public String getNetworkCode() {
        if (networkId.length() > 2) {
            // assume temp net with year appended
            return networkId.substring(0, 2);
        }
        return networkId;
    }

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    public String getLocCode() {
        return locCode;
    }

    public void setLocCode(String locCode) {
        this.locCode = locCode;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    private String networkId;
    private String stationCode;
    private String locCode;
    private String channelCode;
    private Instant startTime;
}

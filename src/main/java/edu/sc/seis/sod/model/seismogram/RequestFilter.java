

package edu.sc.seis.sod.model.seismogram;

import java.time.Duration;
import java.time.Instant;

import edu.sc.seis.seisFile.ChannelTimeWindow;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.model.station.ChannelId;


final public class RequestFilter {
    
    RequestFilter()
    {
    }
    
    public static RequestFilter of(LocalSeismogramImpl seismogram) {
        RequestFilter out = new RequestFilter();
        out.channelId = seismogram.getChannelID();
        out.startTime = seismogram.begin_time;
        out.endTime = seismogram.getEndTime();
        return out;
    }


    public
    RequestFilter(ChannelId channel,
                  Instant start_time,
                  Instant end_time)
    {
        this.channelId = channel;
        this.startTime = start_time;
        this.endTime = end_time;
    }

    public
    RequestFilter(Channel channel,
                  Instant start_time,
                  Instant end_time)
    {
        this.channelId = new ChannelId(channel);
        this.startTime = start_time;
        this.endTime = end_time;
    }
    
    public ChannelTimeWindow asChannelTimeWindow() {
        return new ChannelTimeWindow(channelId.getNetworkId(),
                                     channelId.getStationCode(),
                                     channelId.getLocCode(),
                                     channelId.getChannelCode(),
                                     startTime,
                                     endTime);
    }
    
    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }

    public ChannelId getChannelId() {
        return channelId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public String toString() { return channelId+" "+startTime+" "+endTime; }

    public ChannelId channelId;
    public Instant startTime;
    public Instant endTime;
}

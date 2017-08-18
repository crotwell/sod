package edu.sc.seis.sod.velocity.seismogram;

import java.util.ArrayList;
import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.MicroSecondTimeRange;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;
import edu.sc.seis.sod.velocity.network.VelocityChannel;

/**
 * @author groves Created on May 26, 2005
 */
public class VelocityRequest {

    public VelocityRequest(RequestFilter rf, Channel chan) {
        this(rf, new VelocityChannel((Channel)chan));
    }

    public VelocityRequest(RequestFilter rf, VelocityChannel chan) {
        range = new MicroSecondTimeRange(rf);
        this.chan = chan;
    }

    public MicroSecondDate getBegin() {
        return range.getBeginTime();
    }
    
    public String getBegin(String dateFormat){
        return SimpleVelocitizer.format(getBegin(), dateFormat);
    }

    public MicroSecondDate getEnd() {
        return range.getEndTime();
    }
    
    public String getEnd(String dateFormat){
        return SimpleVelocitizer.format(getEnd(), dateFormat);
    }

    public VelocityChannel getChannel() {
        return chan;
    }

    public String toString() {
        return "Request for " + chan + " from " + getBegin() + " to "
                + getEnd();
    }

    private VelocityChannel chan;

    private MicroSecondTimeRange range;

    public static List<VelocityRequest> wrap(RequestFilter[] original, Channel chan) {
        List<VelocityRequest> results = new ArrayList<VelocityRequest>(original.length);
        for(int i = 0; i < original.length; i++) {
            results.add(new VelocityRequest(original[i], chan));
        }
        return results;
    }
    
    public static List<List<VelocityRequest>> wrap(RequestFilter[][] original, ChannelGroup channelGroup) {
        List<List<VelocityRequest>> results = new ArrayList<List<VelocityRequest>>(original.length);
        for (int i = 0; i < channelGroup.getChannels().length; i++) {
            for (int j = 0; j < original.length; j++) {
                if (original[j].length != 0 && ChannelIdUtil.areEqual(channelGroup.getChannels()[i].getId(), original[j][0].channel_id)) {
                    results.add(wrap(original[j], channelGroup.getChannels()[i]));
                    break;
                }
            }
        }
        return results;
    }
}

package edu.sc.seis.sod.velocity.seismogram;

import java.util.ArrayList;
import java.util.List;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;
import edu.sc.seis.sod.velocity.network.VelocityChannel;

/**
 * @author groves Created on May 26, 2005
 */
public class VelocityRequest {

    public VelocityRequest(RequestFilter rf, Channel chan) {
        this(rf, new VelocityChannel((ChannelImpl)chan));
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

    public static List wrap(RequestFilter[] original, Channel chan) {
        List results = new ArrayList(original.length);
        for(int i = 0; i < original.length; i++) {
            results.add(new VelocityRequest(original[i], chan));
        }
        return results;
    }
}

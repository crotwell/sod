package edu.sc.seis.sod.velocity.seismogram;

import java.util.ArrayList;
import java.util.List;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.sod.velocity.network.VelocityChannel;

/**
 * @author groves Created on May 26, 2005
 */
public class VelocityRequest {

    public VelocityRequest(RequestFilter rf, Channel chan) {
        range = new MicroSecondTimeRange(rf);
        this.chan = new VelocityChannel(chan);
    }

    public MicroSecondDate getStart() {
        return range.getBeginTime();
    }

    public MicroSecondDate getEnd() {
        return range.getEndTime();
    }

    public VelocityChannel getChannel() {
        return chan;
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

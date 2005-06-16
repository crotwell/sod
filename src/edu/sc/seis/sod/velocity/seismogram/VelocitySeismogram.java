package edu.sc.seis.sod.velocity.seismogram;

import java.util.ArrayList;
import java.util.List;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.velocity.network.VelocityChannel;

/**
 * @author groves Created on May 25, 2005
 */
public class VelocitySeismogram extends LocalSeismogramImpl {

    public VelocitySeismogram(LocalSeismogramImpl localSeis, Channel chan) {
        super(localSeis, localSeis.getData());
    }

    public MicroSecondDate getBegin() {
        return getBeginTime();
    }

    public MicroSecondDate getEnd() {
        return getEndTime();
    }

    public VelocityChannel getChannel() {
        return chan;
    }

    public String toString() {
        return "Seismogram on " + getChannel() + " from" + getBegin() + " to "
                + getEnd();
    }

    public static List wrap(LocalSeismogramImpl[] seis, Channel chan) {
        List results = new ArrayList(seis.length);
        for(int i = 0; i < seis.length; i++) {
            results.add(new VelocitySeismogram(seis[i], chan));
        }
        return results;
    }

    private VelocityChannel chan;
}

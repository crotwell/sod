package edu.sc.seis.sod.velocity.seismogram;

import java.util.ArrayList;
import java.util.List;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;
import edu.sc.seis.sod.velocity.network.VelocityChannel;
import edu.sc.seis.sod.velocity.network.VelocitySampling;

/**
 * @author groves Created on May 25, 2005
 */
public class VelocitySeismogram extends LocalSeismogramImpl {

    public VelocitySeismogram(LocalSeismogramImpl localSeis, Channel chan) {
        super(localSeis, localSeis.getData());
        this.chan = VelocityChannel.wrap(chan);
    }

    public MicroSecondDate getBegin() {
        return getBeginTime();
    }

    public String getBegin(String dateFormat) {
        return SimpleVelocitizer.format(getBegin(), dateFormat);
    }

    public MicroSecondDate getEnd() {
        return getEndTime();
    }

    public String getEnd(String dateFormat) {
        return SimpleVelocitizer.format(getEnd(), dateFormat);
    }

    public VelocityChannel getChannel() {
        return chan;
    }

    public SamplingImpl getSampling() {
        return new VelocitySampling(super.getSampling());
    }
    
    public VelocityProperty[] getProps() {
        VelocityProperty[] vProps = new VelocityProperty[properties.length];
        for(int i = 0; i < vProps.length; i++) {
            vProps[i] = new VelocityProperty(properties[i]);
        }
        return vProps;
    }

    public String getAllProps() {
        String out = "Props: ";
        for(int i = 0; i < properties.length; i++) {
            out += "( "+properties[i].name+", "+properties[i].value+" )";
        }
        return out;
    }
    
    public String toString() {
        return "Seismogram on " + getChannel() + " from" + getBegin() + " to "
                + getEnd();
    }

    public static List wrap(LocalSeismogramImpl[] seis, Channel chan) {
        List results = new ArrayList(seis.length);
        chan = VelocityChannel.wrap(chan);
        for(int i = 0; i < seis.length; i++) {
            results.add(VelocitySeismogram.wrap(seis[i], chan));
        }
        return results;
    }

    private VelocityChannel chan;

    public static VelocitySeismogram wrap(LocalSeismogramImpl seis, Channel chan) {
        if(seis instanceof VelocitySeismogram) {
            return (VelocitySeismogram)seis;
        } else {
            return new VelocitySeismogram(seis, chan);
        }
    }
}

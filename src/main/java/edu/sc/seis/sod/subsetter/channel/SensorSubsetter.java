package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.SeismicHardware;
import edu.iris.Fissures.IfNetwork.Sensor;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.source.network.NetworkSource;

/**
 * @author oliverpa
 * 
 * Created on Jul 7, 2005
 */
public abstract class SensorSubsetter extends InstrumentationSubsetter {

    protected SeismicHardware getSeismicHardware(Instrumentation inst) {
        return inst.the_sensor;
    }

    protected boolean acceptNominalHighFreq(ChannelImpl channel,
                                            NetworkSource network,
                                            float nominalHighFreq) {
        try {
            return ((Sensor)getSeismicHardware(channel, network)).nominal_high_freq == nominalHighFreq;
        } catch(ChannelNotFound ex) {
            handleChannelNotFound(ex);
            return false;
        } catch(InvalidResponse ex) {
            handle(ex);
            return false;
        } catch(SodSourceException ex) {
            handle(ex);
            return false;
        }
    }

    protected boolean acceptNominalLowFreq(ChannelImpl channel,
                                           NetworkSource network,
                                           float nominalLowFreq) {
        try {
            return ((Sensor)getSeismicHardware(channel, network)).nominal_low_freq == nominalLowFreq;
        } catch(ChannelNotFound ex) {
            handleChannelNotFound(ex);
            return false;
        } catch(InvalidResponse ex) {
            handle(ex);
            return false;
        } catch(SodSourceException ex) {
            handle(ex);
            return false;
        }
    }
}
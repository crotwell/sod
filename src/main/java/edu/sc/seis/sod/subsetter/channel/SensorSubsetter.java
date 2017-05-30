package edu.sc.seis.sod.subsetter.channel;

import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.Instrumentation;
import edu.sc.seis.sod.model.station.InvalidResponse;
import edu.sc.seis.sod.model.station.SeismicHardwareImpl;
import edu.sc.seis.sod.model.station.SensorImpl;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.source.network.NetworkSource;

/**
 * @author oliverpa
 * 
 * Created on Jul 7, 2005
 */
public abstract class SensorSubsetter extends InstrumentationSubsetter {

    protected SeismicHardwareImpl getSeismicHardware(Instrumentation inst) {
        return inst.the_sensor;
    }

    protected boolean acceptNominalHighFreq(ChannelImpl channel,
                                            NetworkSource network,
                                            float nominalHighFreq) {
        try {
            return ((SensorImpl)getSeismicHardware(channel, network)).nominal_high_freq == nominalHighFreq;
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
            return ((SensorImpl)getSeismicHardware(channel, network)).nominal_low_freq == nominalLowFreq;
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
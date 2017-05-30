package edu.sc.seis.sod.subsetter.channel;

import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.ClockImpl;
import edu.sc.seis.sod.model.station.Instrumentation;
import edu.sc.seis.sod.model.station.InvalidResponse;
import edu.sc.seis.sod.model.station.SeismicHardwareImpl;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.source.network.NetworkSource;


/**
 * @author oliverpa
 * 
 * Created on Jul 7, 2005
 */
public abstract class ClockSubsetter extends InstrumentationSubsetter {

    protected SeismicHardwareImpl getSeismicHardware(Instrumentation inst) {
        return inst.the_clock;
    }
    
    protected boolean acceptType(ChannelImpl channel,
                                 NetworkSource network,
                                 String type) {
        try {
            return type.equals(((ClockImpl)getSeismicHardware(channel, network)).type);
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

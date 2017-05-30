package edu.sc.seis.sod.subsetter.channel;

import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.DataAcqSysImpl;
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
public abstract class DasSubsetter extends InstrumentationSubsetter {

    protected SeismicHardwareImpl getSeismicHardware(Instrumentation inst) {
        return inst.das;
    }

    protected boolean acceptStyle(ChannelImpl channel,
                                  NetworkSource network,
                               int style) {
        try {
            return ((DataAcqSysImpl)getSeismicHardware(channel, network)).style.value() == style;
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

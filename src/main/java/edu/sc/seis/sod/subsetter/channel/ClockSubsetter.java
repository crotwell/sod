package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Clock;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.SeismicHardware;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.source.network.NetworkSource;


/**
 * @author oliverpa
 * 
 * Created on Jul 7, 2005
 */
public abstract class ClockSubsetter extends InstrumentationSubsetter {

    protected SeismicHardware getSeismicHardware(Instrumentation inst) {
        return inst.the_clock;
    }
    
    protected boolean acceptType(Channel channel,
                                 NetworkSource network,
                                 String type) {
        try {
            return type.equals(((Clock)getSeismicHardware(channel, network)).type);
        } catch(ChannelNotFound ex) {
            handleChannelNotFound(ex);
            return false;
        } catch(InvalidResponse ex) {
            handle(ex);
            return false;
        }
    }
}

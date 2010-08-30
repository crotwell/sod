package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.DataAcqSys;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.SeismicHardware;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.source.network.NetworkSource;


/**
 * @author oliverpa
 * 
 * Created on Jul 7, 2005
 */
public abstract class DasSubsetter extends InstrumentationSubsetter {

    protected SeismicHardware getSeismicHardware(Instrumentation inst) {
        return inst.das;
    }

    protected boolean acceptStyle(Channel channel,
                                  NetworkSource network,
                               int style) {
        try {
            return ((DataAcqSys)getSeismicHardware(channel, network)).style.value() == style;
        } catch(ChannelNotFound ex) {
            handleChannelNotFound(ex);
            return false;
        } catch(InvalidResponse ex) {
            handle(ex);
            return false;
        }
    }
    
}

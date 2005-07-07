package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.SeismicHardware;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * @author oliverpa
 * 
 * Created on Jul 7, 2005
 */
public abstract class InstrumentationSubsetter implements ChannelSubsetter {

    protected SeismicHardware getSeismicHardware(Channel channel,
                                                 ProxyNetworkAccess network)
            throws ChannelNotFound {
        ChannelId chanId = channel.get_id();
        return getSeismicHardware(network.retrieve_instrumentation(chanId,
                                                                   chanId.begin_time));
    }

    protected abstract SeismicHardware getSeismicHardware(Instrumentation inst);

    protected boolean acceptId(Channel channel,
                               ProxyNetworkAccess network,
                               int id) {
        try {
            return getSeismicHardware(channel, network).id_number == id;
        } catch(ChannelNotFound ex) {
            handleChannelNotFound(ex);
            return false;
        }
    }

    protected boolean acceptManufacturer(Channel channel,
                                         ProxyNetworkAccess network,
                                         String manufacturer) {
        try {
            return manufacturer.equals(getSeismicHardware(channel, network).manufacturer);
        } catch(ChannelNotFound ex) {
            handleChannelNotFound(ex);
            return false;
        }
    }

    protected boolean acceptModel(Channel channel,
                                  ProxyNetworkAccess network,
                                  String model) {
        try {
            return model.equals(getSeismicHardware(channel, network).model);
        } catch(ChannelNotFound ex) {
            handleChannelNotFound(ex);
            return false;
        }
    }

    protected boolean acceptSerialNumber(Channel channel,
                                         ProxyNetworkAccess network,
                                         String serialNum) {
        try {
            return serialNum.equals(getSeismicHardware(channel, network).serial_number);
        } catch(ChannelNotFound ex) {
            handleChannelNotFound(ex);
            return false;
        }
    }

    protected void handleChannelNotFound(ChannelNotFound ex) {
        GlobalExceptionHandler.handle("channel not found in network, despite the fact that it was found "
                                              + "in the first place to go through this subsetter.  seems "
                                              + "rather silly, eh?",
                                      ex);
    }
}
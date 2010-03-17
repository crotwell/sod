package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.SeismicHardware;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.sod.source.network.NetworkSource;

/**
 * @author oliverpa
 * 
 * Created on Jul 7, 2005
 */
public abstract class InstrumentationSubsetter implements ChannelSubsetter {

    protected SeismicHardware getSeismicHardware(Channel channel,
                                                 NetworkSource network)
            throws ChannelNotFound, InstrumentationInvalid {
        ChannelId chanId = channel.get_id();
        return getSeismicHardware(network.getInstrumentation(chanId));
    }

    protected abstract SeismicHardware getSeismicHardware(Instrumentation inst);

    protected boolean acceptId(Channel channel,
                               NetworkSource network,
                               int id) {
        try {
            return getSeismicHardware(channel, network).id_number == id;
        } catch(ChannelNotFound ex) {
            handleChannelNotFound(ex);
            return false;
        } catch(InstrumentationInvalid ex) {
            handle(ex);
            return false;
        }
    }

    protected boolean acceptManufacturer(Channel channel,
                                         NetworkSource network,
                                         String manufacturer) {
        try {
            return manufacturer.equals(getSeismicHardware(channel, network).manufacturer);
        } catch(ChannelNotFound ex) {
            handleChannelNotFound(ex);
            return false;
        } catch(InstrumentationInvalid ex) {
            handle(ex);
            return false;
        }
    }

    protected boolean acceptModel(Channel channel,
                                  NetworkSource network,
                                  String model) {
        try {
            return model.equals(getSeismicHardware(channel, network).model);
        } catch(ChannelNotFound ex) {
            handleChannelNotFound(ex);
            return false;
        } catch(InstrumentationInvalid ex) {
            handle(ex);
            return false;
        }
    }

    protected boolean acceptSerialNumber(Channel channel,
                                         NetworkSource network,
                                         String serialNum) {
        try {
            return serialNum.equals(getSeismicHardware(channel, network).serial_number);
        } catch(ChannelNotFound ex) {
            handleChannelNotFound(ex);
            return false;
        } catch(InstrumentationInvalid ex) {
            handle(ex);
            return false;
        }
    }

    public static String getChannelNotFoundMsg() {
        return "Channel not found in network, generally indicates no response for this channel in the server.";
    }
    
    public static void handleChannelNotFound(ChannelNotFound ex) {
        logger.info(getChannelNotFoundMsg(),
                                      ex);
    }
    
    public static  String getInstrumentationInvalidMsg() {
        return "Invalid instrumentation ";
    }
    public static void handle(InstrumentationInvalid e) {
        logger.info(getInstrumentationInvalidMsg(), e);
    }
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(InstrumentationSubsetter.class);
}
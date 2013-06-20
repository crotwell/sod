package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.SeismicHardware;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.source.network.NetworkSource;

/**
 * @author oliverpa
 * 
 * Created on Jul 7, 2005
 */
public abstract class InstrumentationSubsetter implements ChannelSubsetter {

    protected SeismicHardware getSeismicHardware(ChannelImpl channel,
                                                 NetworkSource network)
            throws ChannelNotFound, InvalidResponse, SodSourceException {
        return getSeismicHardware(network.getInstrumentation(channel));
    }

    protected abstract SeismicHardware getSeismicHardware(Instrumentation inst);

    protected boolean acceptId(ChannelImpl channel,
                               NetworkSource network,
                               int id) {
        try {
            return getSeismicHardware(channel, network).id_number == id;
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

    protected boolean acceptManufacturer(ChannelImpl channel,
                                         NetworkSource network,
                                         String manufacturer) {
        try {
            return manufacturer.equals(getSeismicHardware(channel, network).manufacturer);
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

    protected boolean acceptModel(ChannelImpl channel,
                                  NetworkSource network,
                                  String model) {
        try {
            return model.equals(getSeismicHardware(channel, network).model);
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

    protected boolean acceptSerialNumber(ChannelImpl channel,
                                         NetworkSource network,
                                         String serialNum) {
        try {
            return serialNum.equals(getSeismicHardware(channel, network).serial_number);
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
    public static void handle(InvalidResponse e) {
        logger.info(getInstrumentationInvalidMsg(), e);
    }
    public static void handle(SodSourceException e) {
        logger.info("Problem loading Instrumentation", e);
    }
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(InstrumentationSubsetter.class);
}

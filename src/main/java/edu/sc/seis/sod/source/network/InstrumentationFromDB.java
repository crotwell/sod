package edu.sc.seis.sod.source.network;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;

public class InstrumentationFromDB extends WrappingNetworkSource implements NetworkSource {

    public InstrumentationFromDB(NetworkSource wrapped) {
        super(wrapped);
    }
    
    @Override
    public Instrumentation getInstrumentation(ChannelId chanId) throws ChannelNotFound, InvalidResponse {
        try {
            return getInstrumentation(NetworkDB.getSingleton().getChannel(chanId));
        } catch(NotFound e) {
            throw new ChannelNotFound(chanId);
        }
    }

    @Override
    public QuantityImpl getSensitivity(ChannelId chanId) throws ChannelNotFound, InvalidResponse {
        Instrumentation inst =  getInstrumentation(chanId);
        return new QuantityImpl(inst.the_response.the_sensitivity.sensitivity_factor,
                                inst.the_response.stages[0].input_units);
    }
    
    public Instrumentation getInstrumentation(ChannelImpl chan) throws ChannelNotFound {
        Instrumentation inst = NetworkDB.getSingleton().getInstrumentation(chan);
        if (inst == null) {
            // null means we have not yet tried to get this instrumentation
            // db throws ChannelNotFound if we tried before and got a ChannelNotFound
            try {
                inst = getWrapped().getInstrumentation(chan.getId());
                NetworkDB.getSingleton().putInstrumentation(chan, inst);
            } catch (ChannelNotFound e) {
                logger.warn(e);
                NetworkDB.getSingleton().putInstrumentation(chan, null);
            } catch(InvalidResponse e) {
                logger.warn(e);
                NetworkDB.getSingleton().putInstrumentation(chan, null);
            }
        }
        return inst;
    }
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(InstrumentationFromDB.class);
    
}

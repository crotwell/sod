package edu.sc.seis.sod.source.network;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;

public class InstrumentationFromDB extends WrappingNetworkSource implements NetworkSource {

    public InstrumentationFromDB(NetworkSource wrapped) {
        super(wrapped);
    }
    
    @Override
    public Instrumentation getInstrumentation(ChannelId chanId) throws ChannelNotFound, InstrumentationInvalid {
        try {
            return getInstrumentation(NetworkDB.getSingleton().getChannel(chanId));
        } catch(NotFound e) {
            throw new ChannelNotFound(chanId);
        }
    }

    @Override
    public Sensitivity getSensitivity(ChannelId chanId) throws ChannelNotFound, InstrumentationInvalid {
        return getInstrumentation(chanId).the_response.the_sensitivity;
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
                NetworkDB.getSingleton().putInstrumentation(chan, null);
            }
        }
        return inst;
    }
    
}

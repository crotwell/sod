package edu.sc.seis.sod.source.network;

import java.util.List;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;

public class InstrumentationFromDB extends NetworkSource {

    public InstrumentationFromDB(NetworkSource wrapped) {
        super(wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public void setConstrainingNetworkCodes(String[] constrainingCodes) {
        super.setConstrainingNetworkCodes(constrainingCodes);
        wrapped.setConstrainingNetworkCodes(constrainingCodes);
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
                inst = wrapped.getInstrumentation(chan.getId());
                NetworkDB.getSingleton().putInstrumentation(chan, inst);
            } catch (ChannelNotFound e) {
                NetworkDB.getSingleton().putInstrumentation(chan, null);
            }
        }
        return inst;
    }
    
    private NetworkSource wrapped;

    @Override
    public List<? extends ChannelImpl> getChannels(StationImpl station) {
        return wrapped.getChannels(station);
    }

    @Override
    public CacheNetworkAccess getNetwork(NetworkAttrImpl attr) {
        return wrapped.getNetwork(attr);
    }

    @Override
    public List<? extends CacheNetworkAccess> getNetworkByName(String name) throws NetworkNotFound {
        return wrapped.getNetworkByName(name);
    }

    @Override
    public List<? extends CacheNetworkAccess> getNetworks() {
        return wrapped.getNetworks();
    }

    @Override
    public List<? extends StationImpl> getStations(NetworkId net) {
        return wrapped.getStations(net);
    }
}

package edu.sc.seis.sod.subsetter.channel;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

/**
 * @author crotwell Created on Jun 3, 2005
 */
public class StationHas extends CompositeChannelSubsetter {

    public StationHas(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(ChannelImpl channel, NetworkSource network)
            throws Exception {
        Iterator it = subsetters.iterator();
        List<ChannelImpl> allChans = network.getChannels((StationImpl)channel.getStation());
        NetworkDB.flush();
        while(it.hasNext()) {
            if(!atLeastOneChannelPasses((ChannelSubsetter)it.next(),
                                        allChans,
                                        network)) {
                NetworkDB.flush();
                return new Fail(this);
            }
        }
        NetworkDB.flush();
        return new Pass(this);
    }

    private static boolean atLeastOneChannelPasses(ChannelSubsetter filter,
                                                   List<ChannelImpl> chans,
                                                   NetworkSource net)
            throws Exception {
        for (ChannelImpl channelImpl : chans) {
            if(filter.accept(channelImpl, net).isSuccess()) {
                return true;
            }
        }
        return false;
    }
}

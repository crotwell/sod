package edu.sc.seis.sod.subsetter.channel;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;
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

    public StringTree accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        Iterator it = subsetters.iterator();
        Channel[] allChans = network.retrieve_for_station(channel.my_site.my_station.get_id());
        while(it.hasNext()) {
            if(!atLeastOneChannelPasses((ChannelSubsetter)it.next(),
                                        allChans,
                                        network)) {
                return new Fail(this);
            }
        }
        return new Pass(this);
    }

    private static boolean atLeastOneChannelPasses(ChannelSubsetter filter,
                                                   Channel[] chans,
                                                   ProxyNetworkAccess net)
            throws Exception {
        for(int i = 0; i < chans.length; i++) {
            if(filter.accept(chans[i], net).isSuccess()) {
                return true;
            }
        }
        return false;
    }
}
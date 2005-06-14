package edu.sc.seis.sod.subsetter.channel;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.sod.ConfigurationException;

/**
 * @author crotwell Created on Jun 3, 2005
 */
public class StationHas extends ChannelLogicalSubsetter implements
        ChannelSubsetter {

    public StationHas(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Channel channel, NetworkAccess network)
            throws Exception {
        Iterator it = subsetters.iterator();
        Channel[] allChans = network.retrieve_for_station(channel.my_site.my_station.get_id());
        while(it.hasNext()) {
            if(!atLeastOneChannelPasses((ChannelSubsetter)it.next(),
                                        allChans,
                                        network)) {
                return false;
            }
        }
        return true;
    }

    private static boolean atLeastOneChannelPasses(ChannelSubsetter filter,
                                                   Channel[] chans,
                                                   NetworkAccess net)
            throws Exception {
        for(int i = 0; i < chans.length; i++) {
            if(filter.accept(chans[i], net)) {
                return true;
            }
        }
        return false;
    }
}
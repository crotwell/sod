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
        if(it.hasNext()) {
            ChannelSubsetter filter = (ChannelSubsetter)it.next();
            Channel[] allChans = network.retrieve_for_station(channel.my_site.my_station.get_id());
            for(int i = 0; i < allChans.length; i++) {
                if(filter.accept(allChans[i], network)) { return true; }
            }
            return false;
        }
        return true;
    }
}
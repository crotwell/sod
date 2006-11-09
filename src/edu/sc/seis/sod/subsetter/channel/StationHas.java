package edu.sc.seis.sod.subsetter.channel;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

/**
 * @author crotwell Created on Jun 3, 2005
 */
public class StationHas extends ChannelAND {

    public StationHas(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        List results = new ArrayList();
        Channel[] allChans = network.retrieve_for_station(channel.my_site.my_station.get_id());
        for(int i = 0; i < allChans.length; i++) {
            StringTree result = super.accept(allChans[i], network);
            results.add(result);
            if(result.isSuccess()) {
                return new StringTreeBranch(this,
                                            true,
                                            (StringTree[])results.toArray(new StringTree[0]));
            }
        }
        return new StringTreeBranch(this,
                                    false,
                                    (StringTree[])results.toArray(new StringTree[0]));
    }

}
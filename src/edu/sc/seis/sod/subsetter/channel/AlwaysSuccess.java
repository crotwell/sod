package edu.sc.seis.sod.subsetter.channel;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.sod.ConfigurationException;

public class AlwaysSuccess extends ChannelLogicalSubsetter {

    public AlwaysSuccess(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Channel channel, NetworkAccess network)
            throws Exception {
        Iterator it = subsetters.iterator();
        while(it.hasNext()) {
            ChannelSubsetter cur = (ChannelSubsetter)it.next();
            if(!cur.accept(channel, network)) {
                break;
            }
        }
        return true;
    }
}

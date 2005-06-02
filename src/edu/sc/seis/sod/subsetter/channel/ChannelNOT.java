package edu.sc.seis.sod.subsetter.channel;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.sod.ConfigurationException;

public final class ChannelNOT extends  ChannelLogicalSubsetter
    implements ChannelSubsetter {

    public ChannelNOT (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Channel e, NetworkAccess network) throws Exception{
        Iterator it = subsetters.iterator();
        if (it.hasNext()) {
            ChannelSubsetter filter = (ChannelSubsetter)it.next();
            if ( filter.accept(e, null)) { return false; }
        }
        return true;
    }

}// ChannelNOT

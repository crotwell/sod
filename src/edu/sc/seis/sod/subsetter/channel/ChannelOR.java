package edu.sc.seis.sod.subsetter.channel;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;

public final class ChannelOR extends  ChannelLogicalSubsetter
    implements ChannelSubsetter {

    public ChannelOR (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Channel e) throws Exception{
        Iterator it = subsetters.iterator();
        while(it.hasNext()) {
            ChannelSubsetter filter = (ChannelSubsetter)it.next();
            if ( filter.accept(e)) { return true; }
        }
        return false;
    }

}// ChannelOR

package edu.sc.seis.sod.subsetter.channel;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;

public final class ChannelAND extends  ChannelLogicalSubsetter
    implements ChannelSubsetter {

    public ChannelAND (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Channel e) throws Exception{
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            ChannelSubsetter filter = (ChannelSubsetter)it.next();
            if ( !filter.accept(e)) { return false; }
        }
        return true;
    }
}// ChannelAND

package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author oliverpa
 * 
 * Created on Jul 7, 2005
 */
public class ClockType extends ClockSubsetter {

    public ClockType(Element config) {
        acceptedType = SodUtil.getNestedText(config);
    }

    public StringTree accept(ChannelImpl channel, NetworkSource network)
            throws Exception {
        return new StringTreeLeaf(this, acceptType(channel, network, acceptedType));
    }

    private String acceptedType;
}
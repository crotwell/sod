package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author oliverpa
 * 
 * Created on Jul 7, 2005
 */
public class SensorId extends SensorSubsetter {

    public SensorId(Element config) {
        id = Integer.parseInt(SodUtil.getNestedText(config));
    }

    public StringTree accept(ChannelImpl channel, ProxyNetworkAccess network) {
        return new StringTreeLeaf(this, acceptId(channel, network, id));
    }

    private int id;
}
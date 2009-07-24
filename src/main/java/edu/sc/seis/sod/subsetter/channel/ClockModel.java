package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author oliverpa
 * 
 * Created on Jul 7, 2005
 */
public class ClockModel extends ClockSubsetter {

    public ClockModel(Element config) {
        acceptedModel = SodUtil.getNestedText(config);
    }

    public StringTree accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        return new StringTreeLeaf(this, acceptModel(channel, network, acceptedModel));
    }

    private String acceptedModel;
}
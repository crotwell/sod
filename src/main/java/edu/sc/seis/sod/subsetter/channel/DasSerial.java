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
public class DasSerial extends DasSubsetter {

    public DasSerial(Element config) {
        acceptedSerial = SodUtil.getNestedText(config);
    }

    public StringTree accept(ChannelImpl channel, ProxyNetworkAccess network)
            throws Exception {
        return new StringTreeLeaf(this, acceptSerialNumber(channel, network, acceptedSerial));
    }

    private String acceptedSerial;
}
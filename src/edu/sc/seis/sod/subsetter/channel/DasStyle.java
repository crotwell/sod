package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.SodUtil;

/**
 * @author oliverpa
 * 
 * Created on Jul 7, 2005
 */
public class DasStyle extends DasSubsetter {

    public DasStyle(Element config) {
        acceptedStyle = Integer.parseInt(SodUtil.getNestedText(config));
    }

    public boolean accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        return acceptStyle(channel, network, acceptedStyle);
    }

    private int acceptedStyle;
}
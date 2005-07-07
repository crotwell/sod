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
public class SensorNominalHighFreq extends SensorSubsetter {

    public SensorNominalHighFreq(Element config) {
        acceptedNominalHighFreq = Float.parseFloat(SodUtil.getNestedText(config));
    }

    public boolean accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        return acceptNominalHighFreq(channel, network, acceptedNominalHighFreq);
    }

    private float acceptedNominalHighFreq;
}

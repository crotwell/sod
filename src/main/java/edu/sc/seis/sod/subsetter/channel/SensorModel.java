package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author oliverpa
 * 
 * Created on Jul 7, 2005
 */
public class SensorModel extends SensorSubsetter {

    public SensorModel(Element config) {
        acceptedModel = SodUtil.getNestedText(config);
    }

    public StringTree accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        boolean accept;
        try {
            accept = acceptedModel.equals(getSeismicHardware(channel, network).model);
            if (accept) {
                return new StringTreeLeaf(this, true);
            } else {
                return new StringTreeLeaf(this, false, getSeismicHardware(channel, network).model);
            }
        } catch(ChannelNotFound ex) {
            return new StringTreeLeaf(this, false, getChannelNotFoundMsg());
        } catch(InstrumentationInvalid ex) {
            return new StringTreeLeaf(this, false, getInstrumentationInvalidMsg());
        }
    }

    private String acceptedModel;
}
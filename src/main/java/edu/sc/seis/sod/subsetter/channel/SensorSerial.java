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
public class SensorSerial extends SensorSubsetter {

    public SensorSerial(Element config) {
        acceptedSerial = SodUtil.getNestedText(config);
    }

    public StringTree accept(ChannelImpl channel, NetworkSource network)
            throws Exception {
        return new StringTreeLeaf(this, acceptSerialNumber(channel, network, acceptedSerial));
    }

    private String acceptedSerial;
}
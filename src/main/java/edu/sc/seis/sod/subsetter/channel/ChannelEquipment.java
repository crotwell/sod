package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Equipment;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractEquipmentSubsetter;

/** regular expression matches of strings for equipment. Can match Type, Manufacturer, Vendor, Model, SerialNumber. */
public class ChannelEquipment extends AbstractEquipmentSubsetter implements ChannelSubsetter {

    public ChannelEquipment() {
    }
    public ChannelEquipment(Element config) {
        super(config);
    }

    @Override
    public StringTree accept(Channel channel, NetworkSource network) throws Exception {
        if (channel.getSensor() != null && super.doesMatch(channel.getSensor())) {
            return new Pass(this);
        }
        if (channel.getDataLogger() != null && super.doesMatch(channel.getDataLogger())) {
            return new Pass(this);
        }
        if (channel.getPreAmplifier() != null && super.doesMatch(channel.getPreAmplifier())) {
            return new Pass(this);
        }
        for (Equipment eq : channel.getEquipment()) {
            if (eq != null && super.doesMatch(eq)) {
                return new Pass(this);
            }  
        }
        return new Fail(this);
    }
}

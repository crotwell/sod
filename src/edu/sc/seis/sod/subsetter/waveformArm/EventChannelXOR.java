package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;
import org.w3c.dom.Element;

public final class EventChannelXOR extends  WaveformLogicalSubsetter
    implements EventChannelSubsetter {

    public EventChannelXOR (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event, Channel channel)
        throws Exception{
        EventChannelSubsetter filterA = (EventChannelSubsetter)filterList.get(0);
        EventChannelSubsetter filterB = (EventChannelSubsetter)filterList.get(1);
        return ( filterA.accept(event, channel) != filterB.accept(event, channel));
    }

}// EventChannelXOR

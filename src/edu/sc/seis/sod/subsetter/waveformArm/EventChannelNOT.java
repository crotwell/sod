package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;
import java.util.Iterator;
import org.w3c.dom.Element;


/**
 * Describe class <code>EventChannelNOT</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public final class EventChannelNOT extends  WaveformLogicalSubsetter
    implements EventChannelSubsetter {

    public EventChannelNOT (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations o, Channel channel)
        throws Exception{
        Iterator it = filterList.iterator();
        while (it.hasNext()) {
            EventChannelSubsetter filter = (EventChannelSubsetter)it.next();
            if (!filter.accept(o, channel)) {return false;}
        }
        return true;
    }
}// EventChannelAND

package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import java.util.Iterator;
import org.w3c.dom.Element;

public final class AvailableDataOR extends  WaveformLogicalSubsetter
    implements AvailableDataSubsetter {

    public AvailableDataOR (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event, Channel channel,
                          RequestFilter[] original, RequestFilter[] available, CookieJar cookieJar)
        throws Exception{
        Iterator it = filterList.iterator();
        while (it.hasNext()) {
            AvailableDataSubsetter filter = (AvailableDataSubsetter)it.next();
            if (filter.accept(event, channel, original, available, cookieJar)) {
                return true;
            }
        }
        return false;
    }
}// AvailableDataOR

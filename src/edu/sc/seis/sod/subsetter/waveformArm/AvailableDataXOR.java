package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import org.w3c.dom.Element;

public final class AvailableDataXOR extends  WaveformLogicalSubsetter
    implements AvailableDataSubsetter {

    public AvailableDataXOR (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event, Channel channel,
                          RequestFilter[] original, RequestFilter[] available, CookieJar cookieJar)
        throws Exception{
        AvailableDataSubsetter filterA = (AvailableDataSubsetter)filterList.get(0);
        AvailableDataSubsetter filterB = (AvailableDataSubsetter)filterList.get(1);
        return ( filterA.accept(event, channel, original, available, cookieJar) != filterB.accept(event, channel, original, available, cookieJar));
    }
}// AvailableDataXOR

package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ConfigurationException;
import org.w3c.dom.Element;

/**
 * BreqFastRequestSubsetter.java
 *
 *
 * Created: Wed Mar 19 14:07:16 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class BreqFastRequestSubsetter implements RequestSubsetter {
    public BreqFastRequestSubsetter(Element config) throws ConfigurationException {
        breqfast = new BreqFastAvailableData(config);
    }

    public boolean accept(EventAccessOperations event, Channel channel,
                          RequestFilter[] request)throws Exception {
        return breqfast.accept(event, channel, request, request);
    }
    BreqFastAvailableData breqfast;
} // BreqFastRequestGenerator

package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.CookieJar;
import org.w3c.dom.Element;


/**
 * NullRequestSubsetter.java
 *
 *
 * Created: Wed Mar 19 16:16:50 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class NullRequestSubsetter implements RequestSubsetter {
    public NullRequestSubsetter() {} // NullRequestSubsetter constructor

    public NullRequestSubsetter(Element config) {}

    public boolean accept(EventAccessOperations event, Channel channel,
                          RequestFilter[] request, CookieJar cookieJar)throws Exception{
        return true;
    }

} // NullRequestSubsetter

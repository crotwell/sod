package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import org.w3c.dom.Element;
/**
 * NullLocalSeismogramSubsetter.java
 *
 *
 * Created: Fri Apr 12 13:41:05 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class NullLocalSeismogramSubsetter implements LocalSeismogramSubsetter{
    public NullLocalSeismogramSubsetter (){

    }

    public NullLocalSeismogramSubsetter(Element config) {
    }

    public boolean accept(EventAccessOperations event,
                          NetworkAccess network,
                          Channel channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          LocalSeismogramImpl[] seismograms,
                          CookieJar cookies) throws Exception {

        return true;

    }

}// LocalSeismogram

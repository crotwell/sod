/**
 * SeismogramNOT.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.process.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import java.util.Iterator;
import org.w3c.dom.Element;

public class SeismogramNOT extends ForkProcess {


    public SeismogramNOT (Element config) throws ConfigurationException {
        super(config);
    }

    public LocalSeismogramResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms,
                                         CookieJar cookieJar
                                        ) throws Exception {
        LocalSeismogramResult result;
        LocalSeismogramProcess processor;
        Iterator it = localSeisProcessList.iterator();
        processor = (LocalSeismogramProcess)it.next();
        synchronized (processor) {
            result = processor.process(event, channel, original,
                                       available, copySeismograms(seismograms), cookieJar);
        }
        return new LocalSeismogramResult( ! result.isSuccess(), result.getSeismograms());
    }
}


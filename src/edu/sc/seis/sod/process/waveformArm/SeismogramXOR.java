/**
 * SeismogramXOR.java
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

public class SeismogramXOR extends ForkProcess {

    public SeismogramXOR (Element config) throws ConfigurationException {
        super(config);
    }

    public LocalSeismogramResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms,
                                         CookieJar cookieJar
                                        ) throws Exception {
        LocalSeismogramResult resultA, resultB;
        LocalSeismogramProcess processorA, processorB;
        Iterator it = localSeisProcessList.iterator();
        processorA = (LocalSeismogramProcess)it.next();
        processorB = (LocalSeismogramProcess)it.next();
        synchronized (processorA) {
            resultA = processorA.process(event, channel, original,
                                       available, copySeismograms(seismograms), cookieJar);
        }
        synchronized (processorB) {
            resultB = processorB.process(event, channel, original,
                                       available, copySeismograms(seismograms), cookieJar);
        }
        if ( resultA.isSuccess() != resultB.isSuccess()) {
            return new LocalSeismogramResult( true, seismograms);
        } else {
            return LocalSeismogramResult.FAIL;
        }
    }
}


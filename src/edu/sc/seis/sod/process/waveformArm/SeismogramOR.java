/**
 * SeismogramOR.java
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

public class SeismogramOR extends ForkProcess {

    public SeismogramOR (Element config) throws ConfigurationException {
        super(config);
    }

    public LocalSeismogramResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms,
                                         CookieJar cookieJar
                                        ) throws Exception {

        LocalSeismogramProcess processor;
        Iterator it = localSeisProcessList.iterator();
        LocalSeismogramResult result = new LocalSeismogramResult(true, seismograms);
        boolean orResult = false;
        // loop until we hit a true, shortcircut, otherwise all are false and FAIL
        while (it.hasNext() && ! orResult) {
            processor = (LocalSeismogramProcess)it.next();
            synchronized (processor) {
                result = processor.process(event, channel, original,
                                           available, copySeismograms(seismograms), cookieJar);
                orResult |= result.isSuccess();
            }
        } // end of while (it.hasNext())
        if (orResult) {
            return new LocalSeismogramResult(orResult, seismograms);
        } else {
            return LocalSeismogramResult.FAIL;
        }
    }
}


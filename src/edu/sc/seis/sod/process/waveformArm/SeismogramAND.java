/**
 * SeismogramAND.java
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

public class SeismogramAND extends ForkProcess {

    public SeismogramAND (Element config) throws ConfigurationException {
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
        while (it.hasNext() && result.isSuccess()) {
            processor = (LocalSeismogramProcess)it.next();
            synchronized (processor) {
                result = processor.process(event, channel, original,
                                           available, copySeismograms(seismograms), cookieJar);
            }
        } // end of while (it.hasNext())
        if (result.isSuccess()) {
            return new LocalSeismogramResult(result.isSuccess(), seismograms);
        } else {
            return LocalSeismogramResult.FAIL;
        }
    }

}


/**
 * SeismogramXOR.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import java.util.Iterator;
import org.w3c.dom.Element;

public class SeismogramXOR extends ForkProcess {

    public SeismogramXOR (Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms,
                                         CookieJar cookieJar
                                        ) throws Exception {
        WaveformResult resultA, resultB;
        WaveformProcess processorA, processorB;
        Iterator it = localSeisProcessList.iterator();
        processorA = (WaveformProcess)it.next();
        processorB = (WaveformProcess)it.next();
        synchronized (processorA) {
            resultA = processorA.process(event, channel, original,
                                         available, copySeismograms(seismograms), cookieJar);
        }
        synchronized (processorB) {
            resultB = processorB.process(event, channel, original,
                                         available, copySeismograms(seismograms), cookieJar);
        }
        boolean xorResult = resultA.isSuccess() != resultB.isSuccess();
            return new WaveformResult(seismograms,
                                             new StringTreeBranch(this,
                                                                  xorResult,
                                                                  new StringTree[] { resultA.getReason(), resultB.getReason() }));

    }
}


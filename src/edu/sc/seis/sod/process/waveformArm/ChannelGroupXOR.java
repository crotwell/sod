/**
 * ChannelGroupXOR.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;
import java.util.Iterator;
import org.w3c.dom.Element;

public class ChannelGroupXOR extends ChannelGroupFork {

    public ChannelGroupXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public ChannelGroupLocalSeismogramResult process(EventAccessOperations event,
                                                     ChannelGroup channelGroup,
                                                     RequestFilter[][] original,
                                                     RequestFilter[][] available,
                                                     LocalSeismogramImpl[][] seismograms,
                                                     CookieJar cookieJar) throws Exception {
        ChannelGroupLocalSeismogramResult resultA, resultB;
        ChannelGroupLocalSeismogramProcess processorA, processorB;
        Iterator it = cgProcessList.iterator();
        processorA = (ChannelGroupLocalSeismogramProcess)it.next();
        processorB = (ChannelGroupLocalSeismogramProcess)it.next();
        synchronized (processorA) {
            resultA = processorA.process(event, channelGroup, original,
                                         available, copySeismograms(seismograms), cookieJar);
        }
        synchronized (processorB) {
            resultB = processorB.process(event, channelGroup, original,
                                         available, copySeismograms(seismograms), cookieJar);
        }
        boolean xorResult = resultA.isSuccess() != resultB.isSuccess();
        return new ChannelGroupLocalSeismogramResult(seismograms,
                                                     new StringTreeBranch(this,
                                                                          xorResult,
                                                                          new StringTree[] { resultA.getReason(), resultB.getReason() }));

    }

}


/**
 * AlwaysSuccess.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.process.waveform.vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.MotionVectorArm;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class WaveformVectorAlwaysSuccess implements
        WaveformVectorProcessWrapper {

    public WaveformVectorAlwaysSuccess(Element config)
            throws ConfigurationException {
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if(node instanceof Element) {
                subProcess = MotionVectorArm.loadAndWrap((Element)node);
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)
    }

    public WaveformVectorResult process(EventAccessOperations event,
                                        ChannelGroup channel,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) {
        try {
            WaveformVectorResult result = subProcess.process(event,
                                                             channel,
                                                             original,
                                                             available,
                                                             seismograms,
                                                             cookieJar);
            return new WaveformVectorResult(result.getSeismograms(),
                                            new StringTreeBranch(this,
                                                                 true,
                                                                 result.getReason()));
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Caught an exception inside Always Success and moving on ...",
                                          e);
            return new WaveformVectorResult(seismograms,
                                            new StringTreeLeaf(this, true));
        }
    }

    public String toString() {
        return "AlwaysSuccess(" + subProcess.toString() + ")";
    }

    WaveformVectorProcess subProcess;

    private static final Logger logger = Logger.getLogger(WaveformVectorAlwaysSuccess.class);

    public WaveformVectorProcess[] getWrappedProcessors() {
        return new WaveformVectorProcess[] {subProcess};
    }
}
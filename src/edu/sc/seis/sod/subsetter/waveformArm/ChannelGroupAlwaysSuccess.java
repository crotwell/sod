/**
 * AlwaysSuccess.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.subsetter.waveformArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.process.waveformArm.ChannelGroupLocalSeismogramProcess;
import edu.sc.seis.sod.process.waveformArm.ChannelGroupLocalSeismogramResult;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.process.waveformArm.LocalSeismogramProcess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.process.waveformArm.ChannelGroupFork;
import edu.sc.seis.sod.process.waveformArm.LocalSeismogramResult;
import edu.sc.seis.sod.process.waveformArm.ForkProcess;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;


public class ChannelGroupAlwaysSuccess implements ChannelGroupLocalSeismogramProcess {

   public ChannelGroupAlwaysSuccess (Element config) throws ConfigurationException {
        NodeList children = config.getChildNodes();
        Node node;
        for (int i=0; i<children.getLength(); i++) {
            node = children.item(i);
            if (node instanceof Element) {
                if (((Element)node).getTagName().equals("description")) {
                    continue;
                }
                Object sodElement = SodUtil.load((Element)node,"waveformArm");
                if(sodElement instanceof ChannelGroupLocalSeismogramProcess) {
                    channelGroupLocalSeisProcess = (ChannelGroupLocalSeismogramProcess)sodElement;
                } else {
                    logger.warn("Unknown tag in ChannelGroupAlwaysSuccess config. " +sodElement);
                }
            }
        }

    }

    public ChannelGroupLocalSeismogramResult process(EventAccessOperations event, ChannelGroup channelGroup, RequestFilter[][] original, RequestFilter[][] available, LocalSeismogramImpl[][] seismograms, CookieJar cookieJar) throws Exception {
        ChannelGroupLocalSeismogramResult result = null;
        try {
            result = channelGroupLocalSeisProcess.process(event, channelGroup, original, available, seismograms, cookieJar);
            return new ChannelGroupLocalSeismogramResult(true, result.getSeismograms(),new StringTreeBranch(this, true, result.getReason()));
        }catch(Exception e) {
            GlobalExceptionHandler.handle("Caught an exception inside ChannelGroupAlwaysSuccess and moving on ...",e);
            return new ChannelGroupLocalSeismogramResult(true, seismograms,new StringTreeBranch(this,true,result.getReason()));
        }

    }


    public String toString() {
        return "ChannelGroupAlwaysSuccess("+channelGroupLocalSeisProcess.toString()+")";
    }


    ChannelGroupLocalSeismogramProcess channelGroupLocalSeisProcess;

    private static final Logger logger = Logger.getLogger(ChannelGroupAlwaysSuccess.class);

}


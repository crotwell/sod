/**
 * AlwaysSuccess.java
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
import edu.sc.seis.sod.SodUtil;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;



public class AlwaysSuccess implements LocalSeismogramProcess {

    public AlwaysSuccess (Element config) throws ConfigurationException {
        NodeList children = config.getChildNodes();
        Node node;
        for (int i=0; i<children.getLength(); i++) {
            node = children.item(i);
            if (node instanceof Element) {
                if (((Element)node).getTagName().equals("description")) {
                    // skip description element
                    continue;
                }
                Object sodElement = SodUtil.load((Element)node,"waveformArm");
                if(sodElement instanceof LocalSeismogramProcess) {
                    localSeisProcess = (LocalSeismogramProcess)sodElement;
                } else {
                    logger.warn("Unknown tag in AlwaysSuccess config. " +sodElement);
                } // end of else
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)

    }



    public LocalSeismogramResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms,
                                         CookieJar cookieJar) {
        try {
            LocalSeismogramResult result = localSeisProcess.process(event, channel, original, available, seismograms, cookieJar);
            return new LocalSeismogramResult(true, result.getSeismograms());
        }catch(Exception e) {
            GlobalExceptionHandler.handle("Caught an exception inside Always Success and moving on ...",e);
            return new LocalSeismogramResult(true, seismograms);
        }

    }


    public String toString() {
        return "AlwaysSuccess("+localSeisProcess.toString()+")";
    }


    LocalSeismogramProcess localSeisProcess;

    private static final Logger logger = Logger.getLogger(AlwaysSuccess.class);

}


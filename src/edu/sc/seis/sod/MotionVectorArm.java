/**
 * MotionVectorArm.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

import edu.sc.seis.sod.subsetter.waveformArm.*;

import edu.sc.seis.sod.process.waveformArm.ChannelGroupLocalSeismogramProcess;
import edu.sc.seis.sod.subsetter.Subsetter;
import java.util.LinkedList;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MotionVectorArm implements Subsetter{
    public MotionVectorArm(Element config) throws ConfigurationException{
        processConfig(config);
    }

    protected void processConfig(Element config)
        throws ConfigurationException {
        NodeList children = config.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element) {
                if (((Element)node).getTagName().equals("description")) {
                    // skip description element
                    continue;
                }
                Object sodElement = SodUtil.load((Element)node,"waveformArm");
                if(sodElement instanceof EventChannelGroupSubsetter) {
                    eventChannelGroup = (EventChannelGroupSubsetter)sodElement;
                } else if(sodElement instanceof ChannelGroupRequestGenerator)  {
                    requestGenerator = (ChannelGroupRequestGenerator)sodElement;
                } else if(sodElement instanceof RequestGenerator)  {
                    requestGenerator = new RequestGeneratorWrapper((RequestGenerator)sodElement);
                } else if(sodElement instanceof ChannelGroupRequestSubsetter)  {
                    request = (ChannelGroupRequestSubsetter)sodElement;
                } else if(sodElement instanceof SeismogramDCLocator)  {
                    dcLocator = (SeismogramDCLocator)sodElement;
                } else if(sodElement instanceof ChannelGroupAvailableDataSubsetter)  {
                    availData = (ChannelGroupAvailableDataSubsetter)sodElement;
                } else if(sodElement instanceof ChannelGroupLocalSeismogramSubsetter)  {
                    seisSubsetter = (ChannelGroupLocalSeismogramSubsetter)sodElement;
                } else if(sodElement instanceof ChannelGroupLocalSeismogramProcess) {
                    processes.add(sodElement);
                } else {
                    logger.warn("Unknown tag in LocalSeismogramArm config. " +sodElement);
                } // end of else
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)

    }

    private EventChannelGroupSubsetter eventChannelGroup = new NullEventChannelSubsetter();

    private ChannelGroupRequestGenerator requestGenerator;

    private ChannelGroupRequestSubsetter request = new NullRequestSubsetter();

    private SeismogramDCLocator dcLocator;

    private ChannelGroupAvailableDataSubsetter availData = new NullAvailableDataSubsetter();

    private ChannelGroupLocalSeismogramSubsetter seisSubsetter = new NullLocalSeismogramSubsetter();

    private LinkedList processes = new LinkedList();

    private static final Logger logger = Logger.getLogger(MotionVectorArm.class);

}


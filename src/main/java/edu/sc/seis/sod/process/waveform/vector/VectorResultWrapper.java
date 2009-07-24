package edu.sc.seis.sod.process.waveform.vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.MotionVectorArm;

public abstract class VectorResultWrapper implements
        WaveformVectorProcessWrapper {

    public VectorResultWrapper(Element config) throws ConfigurationException {
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if(node instanceof Element && !node.getLocalName().equals("classname")) {
                subProcess = MotionVectorArm.loadAndWrap((Element)node);
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)
    }

    public WaveformVectorProcess[] getWrappedProcessors() {
        return new WaveformVectorProcess[] {subProcess};
    }

    protected WaveformVectorProcess subProcess;
}

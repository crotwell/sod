package edu.sc.seis.sod;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.sc.seis.sod.status.waveformArm.WaveformMonitor;


public abstract class AbstractWaveformRecipe  {

    public AbstractWaveformRecipe(Element config) throws ConfigurationException {
        processConfig(config);
    }
    
    public void addStatusMonitor(WaveformMonitor monitor) {
        statusMonitors.add(monitor);
    }

    private Set<WaveformMonitor> statusMonitors = Collections.synchronizedSet(new HashSet<WaveformMonitor>());
    
    protected abstract void handle(Element el) throws ConfigurationException;

    protected void processConfig(Element config) throws ConfigurationException {
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            if(children.item(i) instanceof Element) {
                Element el = (Element)children.item(i);
                    
                
            } // end of if (node instanceof Element)
        } // end of for (intadd i=0; i<children.getSize(); i++)
    }

    public static final String[] PACKAGES = new String[] {"waveformArm",
                                                          "availableData",
                                                          "availableData.vector",
                                                          "eventChannel",
                                                          "eventChannel.vector",
                                                          "eventStation",
                                                          "request",
                                                          "request.vector",
                                                          "requestGenerator",
                                                          "requestGenerator.vector",
                                                          "waveform",
                                                          "waveform.vector",
                                                          "dataCenter"};
}

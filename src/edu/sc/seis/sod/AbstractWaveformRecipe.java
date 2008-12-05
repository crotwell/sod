package edu.sc.seis.sod;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.process.waveform.WaveformProcess;
import edu.sc.seis.sod.status.waveformArm.WaveformMonitor;
import edu.sc.seis.sod.subsetter.eventStation.EventStationSubsetter;


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
                handle(el);
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

    public synchronized void setStatus(CookieEventPair ecp) {
        synchronized(statusMonitors) {
            Iterator<WaveformMonitor> it = statusMonitors.iterator();
            while(it.hasNext()) {
                try {
                    WaveformMonitor monitor = it.next();
                    if(ecp instanceof EventChannelPair) {
                        monitor.update((EventChannelPair)ecp);
                    } else if(ecp instanceof EventVectorPair) {
                        monitor.update((EventVectorPair)ecp);
                    } else if(ecp instanceof EventStationPair) {
                        monitor.update((EventStationPair)ecp);
                    }
                } catch(Exception e) {
                    // oh well, log it and go to next status processor
                    GlobalExceptionHandler.handle("Problem in setStatus", e);
                }
            }
        }
    }

    public synchronized void setStatus(EventNetworkPair ecp) {
        synchronized(statusMonitors) {
            Iterator<WaveformMonitor> it = statusMonitors.iterator();
            while(it.hasNext()) {
                try {
                    it.next().update(ecp);
                } catch(Exception e) {
                    // oh well, log it and go to next status processor
                    GlobalExceptionHandler.handle("Problem in setStatus", e);
                }
            }
        }
    }
    
    public abstract EventStationSubsetter getEventStationSubsetter();
    
    public abstract void add(WaveformProcess proc);
}

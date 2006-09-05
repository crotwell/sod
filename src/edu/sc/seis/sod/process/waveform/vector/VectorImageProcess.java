package edu.sc.seis.sod.process.waveform.vector;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.ComponentSortedSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.configuration.AmpConfigConfiguration;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.display.configuration.SeismogramDisplayConfiguration;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.IndividualizedAmpConfig;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.process.waveform.SeismogramImageProcess;
import edu.sc.seis.sod.process.waveform.SeismogramTitler;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author groves Created on Feb 15, 2005
 */
public class VectorImageProcess extends SeismogramImageProcess implements
        WaveformVectorProcess {

    public VectorImageProcess(Element el) throws Exception {
        super(el);
        NodeList nl = el.getChildNodes();
        for(int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if(n.getNodeName().equals("displayTemplate")) {
                SeismogramDisplayConfiguration.create((Element)n);
            } else if(n.getNodeName().equals("verticalDisplayConfig")) {
                vdc = SeismogramDisplayConfiguration.create((Element)n);
            } else if(n.getNodeName().equals("eastDisplayConfig")) {
                edc = SeismogramDisplayConfiguration.create((Element)n);
            } else if(n.getNodeName().equals("northDisplayConfig")) {
                ndc = SeismogramDisplayConfiguration.create((Element)n);
            } else if(n.getNodeName().equals("globalizedAmpConfig")) {
                globalACConf = AmpConfigConfiguration.create((Element)n);
            }
        }
        if(DOMHelper.hasElement(el, "titler")) {
            titler = new SeismogramTitler(new SeismogramDisplayConfiguration[] {vdc,
                                                                                edc,
                                                                                ndc});
            titler.configure(SodUtil.getElement(el, "titler"));
        }
    }

    public WaveformVectorResult process(EventAccessOperations event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
        Channel chan = channelGroup.getChannels()[0];
        if(titler != null) {
            titler.title(event, chan);
        }
        final ComponentSortedSeismogramDisplay sd = getConfiguredDisplay();
        DataSetSeismogram[] seis = createDataSetSeismograms(event,
                                                            channelGroup,
                                                            original,
                                                            available,
                                                            seismograms);
        populateDisplay(sd, event, channelGroup.getChannels(), seis);
        final String picFileName = locator.getLocation(event, chan);
        final String fileType = locator.getFileType();
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                logger.debug("writing " + picFileName);
                try {
                    if(fileType.equals(PDF)) {
                        sd.setPdfSeismogramsPerPage(1);
                        sd.outputToPDF(new BufferedOutputStream(new FileOutputStream(picFileName)),
                                       true,
                                       false);
                    } else if(fileType.equals(PNG)) {
                        sd.outputToPNG(new File(picFileName), dims);
                    } else {
                        // should never happen
                        throw new RuntimeException("Unknown fileType:"
                                + fileType);
                    }
                } catch(IOException e) {
                    GlobalExceptionHandler.handle("Unable to write to "
                            + picFileName, e);
                }
            }
        });
        return new WaveformVectorResult(seismograms, new StringTreeLeaf(this,
                                                                        true));
    }

    public ComponentSortedSeismogramDisplay getConfiguredDisplay() {
        ComponentSortedSeismogramDisplay sd = new ComponentSortedSeismogramDisplay(false);
        BasicSeismogramDisplay vert = (BasicSeismogramDisplay)vdc.createDisplay();
        sd.setTimeConfig(vert.getTimeConfig());
        sd.setZ(vert);
        sd.setNorth((BasicSeismogramDisplay)ndc.createDisplay());
        sd.setEast((BasicSeismogramDisplay)edc.createDisplay());
        return sd;
    }

    private DataSetSeismogram[] createDataSetSeismograms(EventAccessOperations event,
                                                         ChannelGroup channelGroup,
                                                         RequestFilter[][] original,
                                                         RequestFilter[][] available,
                                                         LocalSeismogramImpl[][] seismograms)
            throws Exception {
        MemoryDataSetSeismogram[] seis = new MemoryDataSetSeismogram[seismograms.length];
        DataSet dataset = new MemoryDataSet("temp",
                                            "Temp Dataset for image creation",
                                            "temp",
                                            new AuditInfo[0]);
        dataset.addParameter(DataSet.EVENT, event, new AuditInfo[0]);
        for(int i = 0; i < seis.length; i++) {
            seis[i] = createSeis(seismograms[i], original[i]);
            dataset.addDataSetSeismogram(seis[i], new AuditInfo[0]);
            dataset.addParameter(DataSet.CHANNEL
                                         + ChannelIdUtil.toString(channelGroup.getChannels()[i].get_id()),
                                 channelGroup.getChannels()[i],
                                 new AuditInfo[0]);
        }
        List seisList = new ArrayList();
        String[] dssNames = dataset.getDataSetSeismogramNames();
        for(int i = 0; i < dssNames.length; i++) {
            seisList.add(dataset.getDataSetSeismogram(dssNames[i]));
        }
        return (DataSetSeismogram[])seisList.toArray(new DataSetSeismogram[0]);
    }

    public void populateDisplay(ComponentSortedSeismogramDisplay sd,
                                EventAccessOperations event,
                                Channel[] chans,
                                DataSetSeismogram[] seis) throws Exception {
        Origin o = EventUtil.extractOrigin(event);
        Arrival[] arrivals = getArrivals(chans[0], o, phaseFlagNames);
        AmpConfig globalAC = null;
        if(globalACConf != null) {
            globalAC = new IndividualizedAmpConfig(globalACConf.createAmpConfig(),
                                                   seis);
        }
        for(int i = 0; i < seis.length; i++) {
            sd.add(new DataSetSeismogram[] {seis[i]});
            if(globalAC != null) {
                sd.get(seis[i]).setAmpConfig(globalAC);
            }
            addFlags(arrivals, o, sd.get(seis[i]), seis[i]);
        }
        if(seis.length > 0) {
            setTimeWindow(sd.getTimeConfig(), phaseWindow, seis[0]);
        }
    }

    public SeismogramTitler getTitler() {
        return titler;
    }

    private AmpConfigConfiguration globalACConf;

    private SeismogramDisplayConfiguration edc = new SeismogramDisplayConfiguration();

    private SeismogramDisplayConfiguration vdc = new SeismogramDisplayConfiguration();

    private SeismogramDisplayConfiguration ndc = new SeismogramDisplayConfiguration();

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(VectorImageProcess.class);
}
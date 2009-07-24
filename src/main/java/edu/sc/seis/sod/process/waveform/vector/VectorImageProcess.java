package edu.sc.seis.sod.process.waveform.vector;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.ComponentSortedSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.configuration.AmpConfigConfiguration;
import edu.sc.seis.fissuresUtil.display.configuration.SeismogramDisplayConfiguration;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.process.waveform.SeismogramImageProcess;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.velocity.network.VelocityChannel;

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
    }

    public WaveformVectorResult process(CacheEvent event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
        DataSetSeismogram[] seis = createDataSetSeismograms(event,
                                                            channelGroup,
                                                            original,
                                                            available,
                                                            seismograms);
        List<VelocityChannel> chanList = new ArrayList<VelocityChannel>();
        chanList.add(new VelocityChannel((ChannelImpl)channelGroup.getChannel1()));
        chanList.add(new VelocityChannel((ChannelImpl)channelGroup.getChannel2()));
        chanList.add(new VelocityChannel((ChannelImpl)channelGroup.getChannel3()));
        ComponentSortedSeismogramDisplay sd = createPopulatedDisplay(event,
                                                                     chanList,
                                                                     seis);
        String picFileName = locator.getLocation(event,
                                                 channelGroup.getChannels()[0]);
        writeImage(sd, locator.getFileType(), picFileName);
        return new WaveformVectorResult(seismograms, new StringTreeLeaf(this,
                                                                        true));
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

    public ComponentSortedSeismogramDisplay createPopulatedDisplay(EventAccessOperations event,
                                                                   List<VelocityChannel> channels,
                                                                   DataSetSeismogram[] seis)
            throws Exception {
        MicroSecondTimeRange timeWindow = null;
        if(seis.length > 0) {
            timeWindow = getTimeWindow(phaseWindow, seis[0]);
            updateTitles(event, channels.iterator().next(), timeWindow);
        }
        ComponentSortedSeismogramDisplay sd = new ComponentSortedSeismogramDisplay(false);
        BasicSeismogramDisplay vert = (BasicSeismogramDisplay)vdc.createDisplay();
        TimeConfig tc = vert.getTimeConfig();
        sd.setTimeConfig(tc);
        sd.setZ(vert);
        sd.setNorth((BasicSeismogramDisplay)ndc.createDisplay());
        sd.setEast((BasicSeismogramDisplay)edc.createDisplay());
        populateDisplay(sd, event, channels, seis);
        if(timeWindow != null) {
            setTimeWindow(tc, timeWindow, tc.getTime(seis[0]));
        }
        return sd;
    }

    private void populateDisplay(ComponentSortedSeismogramDisplay sd,
                                 EventAccessOperations event,
                                 List<VelocityChannel> channels,
                                 DataSetSeismogram[] seis) throws Exception {
        Origin o = EventUtil.extractOrigin(event);
        Arrival[] arrivals = getArrivals(channels.iterator().next(), o, phaseFlagNames);
        AmpConfig globalAC = null;
        if(globalACConf != null) {
            globalAC = globalACConf.createAmpConfig();
            globalAC.add(seis);
        }
        for(int i = 0; i < seis.length; i++) {
            sd.add(new DataSetSeismogram[] {seis[i]});
            if(globalAC != null) {
                sd.get(seis[i]).setAmpConfig(globalAC);
            }
            addFlags(arrivals, o, sd.get(seis[i]), seis[i]);
        }
    }

    private AmpConfigConfiguration globalACConf;

    private SeismogramDisplayConfiguration edc = new SeismogramDisplayConfiguration();

    private SeismogramDisplayConfiguration vdc = new SeismogramDisplayConfiguration();

    private SeismogramDisplayConfiguration ndc = new SeismogramDisplayConfiguration();

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(VectorImageProcess.class);
}
package edu.sc.seis.sod.process.waveform.vector;

import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.w3c.dom.Element;
import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.ComponentSortedSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.VerticalSeismogramDisplay;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.process.waveform.SeismogramImageProcess;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author groves Created on Feb 15, 2005
 */
public class VectorImageProcess extends SeismogramImageProcess
        implements WaveformVectorProcess {

    public VectorImageProcess(Element el) throws Exception {
        super(el);
    }

    public WaveformVectorResult process(EventAccessOperations event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
        logger.debug("process() called");
        Channel chan = channelGroup.getChannels()[0];
        MemoryDataSetSeismogram[] seis = new MemoryDataSetSeismogram[seismograms.length];
        DataSet dataset = new MemoryDataSet("temp",
                                            "Temp Dataset for image creation",
                                            "temp",
                                            new AuditInfo[0]);
        dataset.addParameter(DataSet.EVENT, event, new AuditInfo[0]);
        Origin o = EventUtil.extractOrigin(event);
        Arrival[] arrivals = getArrivals(chan, o, phaseFlagNames);
        final VerticalSeismogramDisplay sd = new ComponentSortedSeismogramDisplay();
        for(int i = 0; i < seis.length; i++) {
            seis[i] = createSeis(seismograms[i],
                                 original[i],
                                 phaseWindow,
                                 event,
                                 channelGroup.getChannels()[i]);
            sd.add(new DataSetSeismogram[] {seis[i]});
            dataset.addDataSetSeismogram(seis[i], new AuditInfo[0]);
            createSodFlags(arrivals, o, sd.get(seis[i]));
        }
        final String picFileName = locator.getLocation(event, chan);
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                logger.debug("writing " + picFileName);
                try {
                    sd.outputToPNG(new File(picFileName), dims);
                } catch(IOException e) {
                    GlobalExceptionHandler.handle("Unable to write to "
                            + picFileName, e);
                }
            }
        });
        return new WaveformVectorResult(seismograms, new StringTreeLeaf(this,
                                                                        true));
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(VectorImageProcess.class);
}
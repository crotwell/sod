package edu.sc.seis.sod.process.waveform;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.log4j.Category;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.event.JDBCEventAccess;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.display.RecordSectionDisplay;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.database.waveform.JDBCRecordSectionChannel;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class RecordSectionDisplayGenerator extends RSChannelInfoPopulator {

    public RecordSectionDisplayGenerator(Element config) throws Exception,
            NoSuchFieldException, ConfigurationException {
        super(config);
        recordSectionChannel = new JDBCRecordSectionChannel();
        eventAccess = new JDBCEventAccess();
        channel = new JDBCChannel();
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel chan,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        super.process(event, chan, original, available, seismograms, cookieJar);
        DataSetSeismogram[] dss = loadSeismograms(event);
        String regionName = ParseRegions.getInstance()
                .getRegionName(event.get_attributes().region);
        String dateTime = event.get_preferred_origin().origin_time.date_time;
        String msg = "Got " + dss.length
                + " DataSetSeismograms from DSML file for event in "
                + regionName + " at " + dateTime;
        logger.debug(msg);
<<<<<<< RecordSectionDisplayGenerator.java
        outputBestRecordSection(event, dss);
=======
        if(displayOption.equals("BEST")) {
            outputBestRecordSection(event, dss);
        } else {
            outputAllRecordSections(event, dss);
        }
>>>>>>> 1.35
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    public void outputBestRecordSection(EventAccessOperations event,
                                        DataSetSeismogram[] dataSeis)
            throws Exception {
        DataSetSeismogram[] bestSeismos = getBestSeismos(dataSeis);
        bestSeismos = wrap(bestSeismos, bestSeismos[0].getDataSet());
        if(bestSeismos != null) {
            writeImage(bestSeismos, event, getFileNameBase()
                    + RSChannelInfoPopulator.fileExtension);
        }
    }

    public void outputBestRecordSection(EventAccessOperations event,
                                        DataSetSeismogram[] dataSeis,
                                        OutputStream out) throws Exception {
        DataSetSeismogram[] bestSeismos = getBestSeismos(dataSeis);
        bestSeismos = wrap(bestSeismos, bestSeismos[0].getDataSet());
        RecordSectionDisplay rsDisplay = getConfiguredRSDisplay();
        rsDisplay.add(bestSeismos);
        rsDisplay.outputToPNG(out, recSecDim);
    }

    private void writeImage(DataSetSeismogram[] dataSeis,
                            EventAccessOperations event,
                            String fileName) throws Exception {
        int eventId = eventAccess.getDBId(event);
        String base = Start.getRunProps().getStatusBaseDir() + "/earthquakes";
        String dir = getSaveSeismogramToFile().getLabel(event);
        new File(base + "/" + dir).mkdirs();
        String fullName = dir + "/" + fileName;
        RecordSectionDisplay rsDisplay = getConfiguredRSDisplay();
        rsDisplay.add(dataSeis);
        logger.debug("Added " + dataSeis.length
                + " seismograms to RecordSectionDisplay");
        try {
            File outPNG = new File(base + "/" + fullName);
            rsDisplay.outputToPNG(outPNG, recSecDim);
        } catch(IOException e) {
            logger.debug("Problem writing recordsection output to PNG", e);
            throw new IOException("Problem writing recordSection output to PNG "
                    + e);
        }
    }

    private JDBCRecordSectionChannel recordSectionChannel;

    private JDBCEventAccess eventAccess;

    private JDBCChannel channel;

    static Category logger = Category.getInstance(RecordSectionDisplayGenerator.class.getName());

    private Dimension recSecDim = new Dimension(500, 500);
}
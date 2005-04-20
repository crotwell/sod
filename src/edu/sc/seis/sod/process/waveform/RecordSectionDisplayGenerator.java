package edu.sc.seis.sod.process.waveform;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Category;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.display.RecordSectionDisplay;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class RecordSectionDisplayGenerator extends RSChannelInfoPopulator {

    public RecordSectionDisplayGenerator(Element config) throws Exception {
        super(config);
        if(DOMHelper.hasElement(config, "fileNameBase")) {
            filename = SodUtil.getText(SodUtil.getElement(config,
                                                          "fileNameBase"))
                    + FILE_EXTENSION;
        }
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel chan,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        super.process(event, chan, original, available, seismograms, cookieJar);
        acceptableChannels.add(ChannelIdUtil.toString(chan.get_id()));
        try {
            DataSetSeismogram[] dss = extractSeismograms(event);
            List acceptableSeis = new ArrayList();
            for(int i = 0; i < dss.length; i++) {
                if(acceptableChannels.contains(ChannelIdUtil.toString(dss[i].getChannelId()))) {
                    acceptableSeis.add(dss[i]);
                }
            }
            String regionName = PR.getRegionName(event.get_attributes().region);
            String dateTime = event.get_preferred_origin().origin_time.date_time;
            String msg = "Got " + dss.length
                    + " DataSetSeismograms from DSML file for event in "
                    + regionName + " at " + dateTime;
            logger.debug(msg);
            dss = (DataSetSeismogram[])acceptableSeis.toArray(new DataSetSeismogram[0]);
            outputBestRecordSection(event, dss);
        } catch(IOException e) {
            logger.debug("Problem opening dsml file in RecordSectionDisplayGenerator",
                         e);
            throw new IOException("Problem opening dsml file in RecordSectionDisplayGenerator"
                    + e);
        }
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    public String getFileLoc(EventAccessOperations event) throws Exception {
        String base = getFileBaseDir();
        String dir = getSaveSeismogramToFile().getLabel(event);
        new File(base + "/" + dir).mkdirs();
        String fileLoc = dir + "/" + filename;
        return fileLoc;
    }

    public static String getFileBaseDir() {
        return Start.getRunProps().getStatusBaseDir() + "/earthquakes";
    }

    public void outputBestRecordSection(EventAccessOperations event,
                                        DataSetSeismogram[] dataSeis)
            throws Exception {
        writeImage(wrap(spacer.spaceOut(dataSeis)), event);
    }

    public void outputBestRecordSection(EventAccessOperations event,
                                        DataSetSeismogram[] dataSeis,
                                        OutputStream out) throws Exception {
        DataSetSeismogram[] bestSeismos = spacer.spaceOut(dataSeis);
        RecordSectionDisplay rsDisplay = getConfiguredRSDisplay();
        rsDisplay.add(wrap(bestSeismos));
        rsDisplay.outputToPNG(out, getRecSecDimension());
    }

    private void writeImage(DataSetSeismogram[] dataSeis,
                            EventAccessOperations event) throws Exception {
        String fileLoc = getFileLoc(event);
        RecordSectionDisplay rsDisplay = getConfiguredRSDisplay();
        rsDisplay.add(dataSeis);
        logger.debug("Added " + dataSeis.length
                + " seismograms to RecordSectionDisplay");
        try {
            File outPNG = new File(getFileBaseDir() + "/" + fileLoc);
            rsDisplay.outputToPNG(outPNG, getRecSecDimension());
        } catch(IOException e) {
            logger.debug("Problem writing recordsection output to PNG", e);
            throw new IOException("Problem writing recordSection output to PNG "
                    + e);
        }
    }

    private String filename = "recordsection" + FILE_EXTENSION;

    private Set acceptableChannels = new HashSet();

    private static final String FILE_EXTENSION = ".png";

    private static final ParseRegions PR = ParseRegions.getInstance();

    private static Category logger = Category.getInstance(RecordSectionDisplayGenerator.class.getName());
}
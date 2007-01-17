package edu.sc.seis.sod.process.waveform;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import org.apache.log4j.Category;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
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
        this(config, ConnMgr.createConnection());
    }

    public RecordSectionDisplayGenerator(Element config, Connection conn)
            throws Exception {
        super(config, conn);
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
        if(!updateTable(event, chan, original, available, seismograms, cookieJar)){
            return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
        }
        makeRecordSection(event);
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    public void makeRecordSection(EventAccessOperations event) throws Exception, NoPreferredOrigin, IOException {
        try {
            DataSetSeismogram[] dss = extractSeismograms(event);
            String regionName = PR.getRegionName(event.get_attributes().region);
            String dateTime = event.get_preferred_origin().origin_time.date_time;
            String msg = "Got " + dss.length
                    + " DataSetSeismograms from DSML file for event in "
                    + regionName + " at " + dateTime;
            logger.debug(msg);
            outputBestRecordSection(event, dss);
        } catch(IOException e) {
            throw new IOException("Problem opening dsml file in RecordSectionDisplayGenerator"
                    + e);
        }
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
        if(spacer != null) {
            writeImage(wrap(spacer.spaceOut(dataSeis)), event);
        } else {
            writeImage(wrap(dataSeis), event);
        }
    }

    public void outputRecordSection(DataSetSeismogram[] dataSeis,
                                    OutputStream out) throws Exception {
        RecordSectionDisplay rsDisplay = getConfiguredRSDisplay();
        rsDisplay.add(wrap(dataSeis));
        rsDisplay.outputToPNG(out, getRecSecDimension());
    }

    protected void writeImage(DataSetSeismogram[] dataSeis,
                              EventAccessOperations event) throws Exception {
        String fileLoc = getFileLoc(event);
        RecordSectionDisplay rsDisplay = getConfiguredRSDisplay();
        rsDisplay.add(dataSeis);
        if(dataSeis.length > 0) {
            SeismogramImageProcess.setTimeWindow(rsDisplay.getTimeConfig(),
                                                 dataSeis[0]);
        }
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

    protected String filename = "recordsection" + FILE_EXTENSION;

    protected static final String FILE_EXTENSION = ".png";

    private static final ParseRegions PR = ParseRegions.getInstance();

    private static Category logger = Category.getInstance(RecordSectionDisplayGenerator.class.getName());
}
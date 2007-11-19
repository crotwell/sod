package edu.sc.seis.sod.process.waveform;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
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
        super(config);
        if(DOMHelper.hasElement(config, "fileNameBase")) {
            filename = SodUtil.getText(SodUtil.getElement(config,
                                                          "fileNameBase"))
                    + FILE_EXTENSION;
        }
        if(DOMHelper.hasElement(config, "baseDir")) {
            baseDirName = SodUtil.getText(SodUtil.getElement(config, "baseDir"));
        }
    }

    public WaveformResult process(CacheEvent event,
                                  Channel chan,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        boolean out = updateTable(event,
                                  chan,
                                  original,
                                  available,
                                  seismograms,
                                  cookieJar);
        if(!out) {
            return new WaveformResult(seismograms,
                                      new StringTreeLeaf(this, out));
        }
        makeRecordSection(event);
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    public void makeRecordSection(CacheEvent event)
            throws Exception, NoPreferredOrigin, IOException {
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

    public String getFileBaseDir() {
        return Start.getRunProps().getStatusBaseDir() + '/' + baseDirName;
    }

    public String getBaseDirName() {
        return baseDirName;
    }

    public void outputBestRecordSection(EventAccessOperations event,
                                        DataSetSeismogram[] dataSeis)
            throws Exception {
        if(spacer != null) {
            writeImage(wrap(spacer.spaceOut(dataSeis)), event, false);
        } else {
            writeImage(wrap(dataSeis), event, false);
        }
    }
    
    public void outputRecordSection(DataSetSeismogram[] dataSeis,
                                    EventAccessOperations event,
                                    OutputStream out) throws Exception {
        outputRecordSection(dataSeis, event, out, false);
    }

    public void outputRecordSection(DataSetSeismogram[] dataSeis,
                                    EventAccessOperations event,
                                    OutputStream out, 
                                    boolean isPDF) throws Exception {
        writeImage(wrap(dataSeis), event, out, isPDF);
    }

    protected void writeImage(DataSetSeismogram[] dataSeis,
                              EventAccessOperations event,
                              OutputStream out,
                              boolean isPDF) throws Exception {
        RecordSectionDisplay rsDisplay = getConfiguredRSDisplay();
        rsDisplay.add(dataSeis);
        if(dataSeis.length > 0) {
            SeismogramImageProcess.setTimeWindow(rsDisplay.getTimeConfig(),
                                                 dataSeis[0]);
        }
        logger.debug("Added " + dataSeis.length
                + " seismograms to RecordSectionDisplay");
        if (isPDF) {
            rsDisplay.outputToPDF(out);
        } else {
            rsDisplay.outputToPNG(out, getRecSecDimension());
        }
    }

    protected void writeImage(DataSetSeismogram[] dataSeis,
                              EventAccessOperations event,
                              boolean isPDF) throws Exception {
        String fileLoc = getFileLoc(event);
        try {
            File outFile = new File(getFileBaseDir() + "/" + fileLoc);
            writeImage(dataSeis,
                       event,
                       new BufferedOutputStream(new FileOutputStream(outFile)),
                       isPDF);
        } catch(IOException e) {
            String msg = "Problem writing recordsection output to image file ";
            logger.debug(msg, e);
            throw new IOException(msg + e);
        }
    }
    
    protected String filename = "recordsection" + FILE_EXTENSION;

    protected String baseDirName = DEFAULT_BASE_DIRNAME;

    protected static final String FILE_EXTENSION = ".png";

    protected static final String DEFAULT_BASE_DIRNAME = "earthquakes";

    private static final ParseRegions PR = ParseRegions.getInstance();

    private static Category logger = Category.getInstance(RecordSectionDisplayGenerator.class.getName());
}
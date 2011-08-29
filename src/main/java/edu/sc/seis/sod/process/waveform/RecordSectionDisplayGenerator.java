package edu.sc.seis.sod.process.waveform;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.display.RecordSectionDisplay;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.RecordSectionItem;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

public class RecordSectionDisplayGenerator extends RSChannelInfoPopulator {

    public RecordSectionDisplayGenerator(Element config) throws Exception {
        super(config);
        if(DOMHelper.hasElement(config, "fileNameBase")) {
            filename = SodUtil.getText(SodUtil.getElement(config,
                                                          "fileNameBase"))
                    + FILE_EXTENSION;
        }
        if(DOMHelper.hasElement(config, "workingDir")) {
            workingDirName = SodUtil.getText(SodUtil.getElement(config, "workingDir"));
        }
        if(DOMHelper.hasElement(config, "location")) {
            location = SodUtil.getText(SodUtil.getElement(config, "location"));
        }
    }

    public WaveformResult accept(CacheEvent event,
                                  ChannelImpl chan,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        logger.debug("BEGIN RecordSectionDisplay accept ");
        List<RecordSectionItem> best = updateTable(event,
                                  chan,
                                  original,
                                  available,
                                  seismograms,
                                  cookieJar);
        if(best.size() == 0) {
            logger.debug("END RecordSectionDisplay accept best=0 ");
            return new WaveformResult(seismograms,
                                      new StringTreeLeaf(this, false));
        }
        writeImage(getDSSForRecordSectionItems(best), event, false);
        logger.debug("END RecordSectionDisplay accept write image");
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    public String getFileLoc(EventAccessOperations event) throws Exception {
        String base = getFileBaseDir();
        String dir = velocitizer.evaluate(location, event);
        new File(base + "/" + dir).mkdirs();
        String fileLoc = dir + "/" + filename;
        return fileLoc;
    }

    public String getFileBaseDir() {
        return Start.getRunProps().getStatusBaseDir() + '/' + workingDirName;
    }

    public String getBaseDirName() {
        return workingDirName;
    }

    public void writeImage(List<MemoryDataSetSeismogram> dataSeis,
                              EventAccessOperations event,
                              OutputStream out,
                              boolean isPDF) throws Exception {
        RecordSectionDisplay rsDisplay = getConfiguredRSDisplay();
        rsDisplay.clear();
        rsDisplay.add(dataSeis.toArray(new DataSetSeismogram[0]));
        if(dataSeis.size() > 0) {
            SeismogramImageProcess.setTimeWindow(rsDisplay.getTimeConfig(),
                                                 dataSeis.get(0));
        }
        if (dataSeis.get(0).getChannelId().channel_code.endsWith("Z")) {
            logger.debug("Added " + dataSeis.size()
                         + " seismograms to RecordSectionDisplay");
        }
        if (isPDF) {
            rsDisplay.outputToPDF(out);
        } else {
            rsDisplay.outputToPNG(out, getRecSecDimension());
        }
    }

    public void writeImage(List<MemoryDataSetSeismogram> dataSeis,
                              EventAccessOperations event,
                              boolean isPDF) throws Exception {
        String fileLoc = getFileLoc(event);
        if (dataSeis.get(0).getChannelId().channel_code.endsWith("Z")) {
            logger.debug("RecordSection writeImage: "+fileLoc);
            for (int i = 0; i < dataSeis.size(); i++) {
                logger.debug("RecordSection writeImage: "+i+" "+dataSeis.get(i).getName());
            }
        }
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

    protected String workingDirName = DEFAULT_BASE_DIRNAME;
    
    protected String location = DEFAULT_TEMPLATE;
    
    private SimpleVelocitizer velocitizer = new SimpleVelocitizer();
    
    public static final String DEFAULT_TEMPLATE = "Event_$event.getTime('yyyy_DDD_HH_mm_ss')";

    protected static final String FILE_EXTENSION = ".png";

    protected static final String DEFAULT_BASE_DIRNAME = "earthquakes";

    private static final ParseRegions PR = ParseRegions.getInstance();

    private static Logger logger = LoggerFactory.getLogger(RecordSectionDisplayGenerator.class.getName());
}

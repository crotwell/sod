package edu.sc.seis.sod.process.waveform;

import edu.sc.seis.fissuresUtil.xml.DataSet;
import java.net.URL;
import edu.sc.seis.fissuresUtil.xml.DataSetToXML;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.*;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.RecordSectionDisplay;
import org.apache.log4j.BasicConfigurator;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import org.w3c.dom.Element;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.sod.status.StringTreeLeaf;
import java.awt.Dimension;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.fissuresUtil.xml.IncomprehensibleDSMLException;
import edu.sc.seis.fissuresUtil.xml.UnsupportedFileTypeException;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.process.waveform.vector.WaveformVectorProcess;
import edu.sc.seis.sod.status.waveformArm.WaveformMonitor;

public class RecordSectionDisplayGenerator implements WaveformProcess {

    public RecordSectionDisplayGenerator(Element config)
            throws ConfigurationException {
        this.config = config;
        this.id = SodUtil.getText(SodUtil.getElement(config, "id"));
    }

    public SaveSeismogramToFile getSaveSeismogramToFile()
            throws ConfigurationException {
        WaveformProcess[] waveformProcesses = Start.getWaveformArm()
                .getLocalSeismogramArm()
                .getProcesses();
        for(int i = 0; i < waveformProcesses.length; i++) {
            if(waveformProcesses[i] instanceof SaveSeismogramToFile) {
                SaveSeismogramToFile saveSeis = (SaveSeismogramToFile)waveformProcesses[i];
                if(id.equals(saveSeis.getId())) { return saveSeis; }
            }
        }
        throw new ConfigurationException("RecordSectionDisplayGenerator needs a SaveSeismogramToFile process");
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar)
            throws ParserConfigurationException, IOException,
            IncomprehensibleDSMLException, UnsupportedFileTypeException,
            ConfigurationException {
        try {
            fileNameCounter = 1;
            saveSeisToFile = getSaveSeismogramToFile();
            DataSet ds = DataSetToXML.load(saveSeisToFile.getDSMLFile(event)
                    .toURI()
                    .toURL());
            String[] dataSeisNames = ds.getDataSetSeismogramNames();
            int numDSSeismograms = dataSeisNames.length;
            dss = new DataSetSeismogram[numDSSeismograms];
            for(int i = 0; i < numDSSeismograms; i++) {
                dss[i] = ds.getDataSetSeismogram(dataSeisNames[i]);
            }
            outputRecordSections(event, dss);
        } catch(IOException e) {
            throw new IOException("Problem opening dsml file in RecordSectionDisplayGenerator"
                    + e);
        }
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    public void sort(DataSetSeismogram[] dataSeis) {
        for(int i = 0; i < dataSeis.length; i++) {
            QuantityImpl distance = DisplayUtils.calculateDistance(dataSeis[i]);
            for(int j = 0; j < dataSeis.length; j++) {
                if(distance.lessThan(DisplayUtils.calculateDistance(dataSeis[j]))) {
                    DataSetSeismogram tempSeis = dataSeis[i];
                    dataSeis[i] = dataSeis[j];
                    dataSeis[j] = tempSeis;
                }
            }
        }
    }

    private void outputRecordSections(EventAccessOperations event,
                                      DataSetSeismogram[] dataSeis)
            throws IOException {
        int length = dataSeis.length;
        sort(dataSeis);
        RecordSectionDisplay recordSectionDisplay = new RecordSectionDisplay();
        if(length > 0 && length <= 6) {
            writeImage(dss, event);
            return;
        } else {
            DataSetSeismogram[] tempDSS = new DataSetSeismogram[6];
            int maxIndex = 0;
            int spacing = length / 6;
            for(int i = 0; i < spacing; i++) {
                int j = 0;
                for(int seisCount=0;seisCount<6;seisCount++){
                    maxIndex = j + i;
                    tempDSS[seisCount] = dataSeis[maxIndex];
                    j += spacing;
                }
                writeImage(tempDSS, event);
            }
            if((length % 6) != 0) {
                tempDSS = new DataSetSeismogram[length - maxIndex - 1];
                for(int k = 0; k < length - maxIndex - 1; k++) {
                    tempDSS[k] = dataSeis[maxIndex + k + 1];
                }
                if((length - maxIndex - 1) > 0) {
                    writeImage(tempDSS, event);
                }
            }
        }
    }

    private void writeImage(DataSetSeismogram[] dataSeis,
                            EventAccessOperations event) throws IOException {
        File parentDir = saveSeisToFile.getEventDirectory(event);
        RecordSectionDisplay rsDisplay = new RecordSectionDisplay();
        rsDisplay.add(dataSeis);
        try {
            File outPNG = new File(parentDir, fileBase + fileNameCounter
                    + fileExtension);
            rsDisplay.outputToPNG(outPNG, new Dimension(500, 500));
            fileNameCounter++;
        } catch(IOException e) {
            throw new IOException("Problem writing recordSection output to PNG "
                    + e);
        }
    }

    public DataSetSeismogram[] dss;

    private SaveSeismogramToFile saveSeisToFile;

    private Element config;

    private String id;

    private int counter;

    private int fileNameCounter;

    private final static String fileBase = "recordSection";

    private final static String fileExtension = ".png";
}
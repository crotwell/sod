package edu.sc.seis.sod.process.waveform;

import edu.sc.seis.fissuresUtil.xml.DataSet;
import java.sql.SQLException;
import edu.sc.seis.fissuresUtil.xml.DataSetToXML;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.*;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.event.JDBCEventAccess;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.RecordSectionDisplay;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import org.w3c.dom.Element;
import edu.sc.seis.sod.status.StringTreeLeaf;
import java.awt.Dimension;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.fissuresUtil.xml.IncomprehensibleDSMLException;
import edu.sc.seis.fissuresUtil.xml.UnsupportedFileTypeException;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.database.waveform.JDBCEventRecordSection;
import edu.sc.seis.sod.database.waveform.JDBCRecordSectionChannel;

public class RecordSectionDisplayGenerator implements WaveformProcess {

    public RecordSectionDisplayGenerator(Element config)
            throws ConfigurationException, SQLException {
        this.id = SodUtil.getText(SodUtil.getElement(config, "id"));
        this.displayOption = SodUtil.getText(SodUtil.getElement(config,
                                                                "displayOption"));
        this.fileNameBase = SodUtil.getText(SodUtil.getElement(config,
                                                               "fileNameBase"));
        this.numSeisPerImage = new Integer(SodUtil.getText(SodUtil.getElement(config,
                                                                              "numSeisPerRecordSection"))).intValue();
        String eventRecSecTableName="eventRecordSection" + this.id;
        String recSecChanTableName="recSecChannel"+ this.id;
        recordSectionChannel = new JDBCRecordSectionChannel(recSecChanTableName,eventRecSecTableName);
        eventRecordSection = new JDBCEventRecordSection(eventRecSecTableName,recSecChanTableName);
        eventAccess = new JDBCEventAccess();
        channel = new JDBCChannel();
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
            ConfigurationException, NotFound, SQLException {
        try {
            saveSeisToFile = getSaveSeismogramToFile();
            DataSet ds = DataSetToXML.load(saveSeisToFile.getDSMLFile(event)
                    .toURI()
                    .toURL());
            String[] dataSeisNames = ds.getDataSetSeismogramNames();
            int numDSSeismograms = dataSeisNames.length;
            DataSetSeismogram[] dss = new DataSetSeismogram[numDSSeismograms];
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
                if(distance.greaterThan(DisplayUtils.calculateDistance(dataSeis[j]))) {
                    DataSetSeismogram tempSeis = dataSeis[i];
                    dataSeis[i] = dataSeis[j];
                    dataSeis[j] = tempSeis;
                }
            }
        }
    }

    public void outputRecordSections(EventAccessOperations event,
                                     DataSetSeismogram[] dataSeis)
            throws IOException, SQLException, NotFound {
        int length = dataSeis.length;
        boolean bestDisplay = false;
        if(displayOption.equals("BEST")) {
            bestDisplay = true;
        }
        if(length > 0) {
            int fileNameCounter = 0;
            if(length <= numSeisPerImage) {
                writeImage(dataSeis, event, fileNameBase + fileNameCounter + fileExtension);
                return;
            } else {
                sort(dataSeis);
                DataSetSeismogram[] tempDSS;
                int spacing = length / numSeisPerImage;
                int imageCount = bestDisplay ? 1 : spacing;
                for(int i = 0; i < imageCount; i++) {
                    tempDSS = new DataSetSeismogram[numSeisPerImage];
                    int index = i;
                    for(int j = 0; j < numSeisPerImage; j++) {
                        tempDSS[j] = dataSeis[index];
                        index += spacing;
                    }
                    //String fileName = (i == 1 ? (fileNameBase + fileExtension)
                      //      : (fileNameBase + fileNameCounter + fileExtension));
                    writeImage(tempDSS, event, fileNameBase + fileNameCounter + fileExtension);
                    fileNameCounter++;
                }
                if(!bestDisplay) {
                    if((length % numSeisPerImage) != 0) {
                        int maxIndex = numSeisPerImage * spacing - 1;
                        tempDSS = new DataSetSeismogram[length - maxIndex - 1];
                        for(int k = 0; k < length - maxIndex - 1; k++) {
                            tempDSS[k] = dataSeis[maxIndex + k + 1];
                        }
                        writeImage(tempDSS, event, fileNameBase
                                + fileNameCounter + fileExtension);
                    }
                }
            }
        }
    }

    private void writeImage(DataSetSeismogram[] dataSeis,
                            EventAccessOperations event,
                            String fileName) throws IOException, NotFound,
            SQLException {
        int recSecId=-1;
        int eventId = eventAccess.getDBId(event);
        boolean isBest = false;
        if(displayOption.equals("BEST")) {
            isBest = true;
        }
        if(!eventRecordSection.imageExists(eventId,fileName)){
            recSecId=eventRecordSection.insert(eventId,fileName,isBest);
        }else{
            recSecId=eventRecordSection.getRecSecId(eventId,fileName);
        }
        File parentDir = saveSeisToFile.getEventDirectory(event);
        RecordSectionDisplay rsDisplay = new RecordSectionDisplay();
        rsDisplay.add(dataSeis);
        try {
            File outPNG = new File(parentDir, fileName);
            rsDisplay.outputToPNG(outPNG, new Dimension(500, 500));
            for(int j=0;j<dataSeis.length;j++){
                int channelId=channel.getDBId(dataSeis[j].getRequestFilter().channel_id);
                if(recordSectionChannel.channelExists(eventId,channelId)){
                    recordSectionChannel.updateRecordSection(recSecId,eventId,channelId);
                }else{
                    recordSectionChannel.insert(recSecId,channelId);
                }
            }
        } catch(IOException e) {
            throw new IOException("Problem writing recordSection output to PNG "
                    + e);
        }
    }

    private SaveSeismogramToFile saveSeisToFile;

    private String id;

    private int numSeisPerImage = 6;

    private String displayOption = "";

    private JDBCEventRecordSection eventRecordSection;

    private JDBCRecordSectionChannel recordSectionChannel;

    private JDBCEventAccess eventAccess;
    
    private JDBCChannel channel;

    private String fileNameBase = "RecordSection";//default

    private final static String fileExtension = ".png";
}
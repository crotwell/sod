
package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.fissuresUtil.xml.*;

import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.sac.FissuresToSac;
import edu.sc.seis.fissuresUtil.sac.SacTimeSeries;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.LocalSeismogramProcess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.NameGenerator;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Category;
import org.w3c.dom.Element;

/**
 * SacFileProcessor.java
 * A example config Element is:<br>
 * <pre>
 * &lt;SacFileProcessor>
 *    &lt;dataDirectory>research/sodtest/data&lt;/dataDirectory>
 * &lt;/SacFileProcessor>
 * </pre>
 *
 *
 * Created: Tue Mar 19 14:08:39 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public class SacFileProcessor implements LocalSeismogramProcess {
    /**
     * Creates a new <code>SacFileProcessor</code> instance.
     *
     * @param config an <code>Element</code> that contains the configuration
     * for this Processor
     */
    public SacFileProcessor (Element config) throws ConfigurationException {
        this.config = config;
        regions = new ParseRegions();
        String datadirName =
            SodUtil.getText(SodUtil.getElement(config, "dataDirectory"));
        this.dataDirectory = new File(datadirName);
        if ( ! dataDirectory.exists()) {
            if ( ! dataDirectory.mkdirs()) {
                throw new ConfigurationException("Unable to create directory."+dataDirectory);
            } // end of if (!)

        } // end of if (dataDirectory.exits())

    }

    /**
     * Describe <code>process</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param original a <code>RequestFilter[]</code> value
     * @param available a <code>RequestFilter[]</code> value
     * @param seismograms a <code>LocalSeismogram[]</code> value
     * @param cookies a <code>CookieJar</code> value
     * @exception Exception if an error occurs
     */
    public LocalSeismogram[] process(EventAccessOperations event,
                                     NetworkAccess network,
                                     Channel channel,
                                     RequestFilter[] original,
                                     RequestFilter[] available,
                                     LocalSeismogram[] seismograms,
                                     CookieJar cookies) throws Exception {

        logger.info("Got "+seismograms.length+" seismograms for "+
                        ChannelIdUtil.toString(channel.get_id())+
                        " for event in "+
                        regions.getRegionName(event.get_attributes().region)+
                        " at "+event.get_preferred_origin().origin_time.date_time);
        DataSet dataset = getDataSet(event);
        saveInDataSet(dataset, event, channel, seismograms);
        saveDataSet(dataset);

        return seismograms;
    }

    protected URLDataSetSeismogram saveInDataSet(DataSet dataset,
                                                 EventAccessOperations event,
                                                 Channel channel,
                                                 LocalSeismogram[] seismograms)
        throws ConfigurationException,
        CodecException,
        IOException,
        NoPreferredOrigin {

        File eventDirectory = getEventDirectory(event);
        LocalSeismogramImpl[] lseis = new LocalSeismogramImpl[seismograms.length];
        for (int i = 0; i < lseis.length; i++) {
            lseis[i] = (LocalSeismogramImpl)seismograms[i];
        }
        AuditInfo[] audit = new AuditInfo[1];
        audit[0] = new AuditInfo(System.getProperty("user.name"),
                                 "seismogram loaded via sod.");
        URLDataSetSeismogram urlDSS = URLDataSetSeismogram.saveLocally(dataset,
                                                                       eventDirectory,
                                                                       lseis,
                                                                       channel,
                                                                       event,
                                                                       audit);


        return urlDSS;
    }

    protected void saveDataSet(DataSet ds)
        throws IOException, ParserConfigurationException, MalformedURLException, ConfigurationException {

        //            File outFile = new File(eventDirectory, eventDirName+".dsml");
        //            OutputStream fos = new BufferedOutputStream(
        //                new FileOutputStream(outFile));
        //            dataset.write(fos);
        //            fos.close();
        DataSetToXML dsToXML = new DataSetToXML();
        File outFile = dsToXML.save(ds, getEventDirectory(ds.getEvent()));
        logger.debug("DSML saved to "+outFile.getName());
        long mbyte = 1024*1024;
        Runtime runtime = Runtime.getRuntime();
        String s = "Memory usage: "+runtime.freeMemory()/mbyte+" "+
            runtime.totalMemory()/mbyte+"/"+runtime.maxMemory()/mbyte+" Mb";
        System.out.println(s);
    }

    protected File getEventDirectory(EventAccessOperations event)
        throws ConfigurationException {
        String eventDirName = getLabel(event);
        File eventDirectory = new File(dataDirectory, eventDirName);
        if ( ! eventDirectory.exists()) {
            if ( ! eventDirectory.mkdirs()) {
                throw new ConfigurationException("Unable to create directory."+eventDirectory);
            } // end of if (!)
        } // end of if (dataDirectory.exits())

        return eventDirectory;
    }

    protected DataSet getDataSet(EventAccessOperations event)
        throws NoPreferredOrigin, ConfigurationException {
        DataSet dataset;

        File eventDirectory = getEventDirectory(event);

        // assume that processing is in event order and never reopens
        // bad but just temporary
        if (lastDataSet != null && lastDataSet.getEvent().equals(event)) {
            dataset = lastDataSet;
        } else {
            //temp
            dataset = new MemoryDataSet(event.get_preferred_origin().origin_time.date_time,
                                        event.get_attributes().region.number+":"+
                                            event.get_preferred_origin().magnitudes[0].value,
                                        System.getProperty("user.name"),
                                        new AuditInfo[0]);
            dataset.addParameter(dataset.EVENT, event, new AuditInfo[0]);
            lastDataSet = dataset;
            //dataset = getDataSet(eventDirectory, eventDirName, event);
        }
        return dataset;
    }

    protected DataSet getXMLDataSet(EventAccessOperations event)
        throws MalformedURLException, ParserConfigurationException, ConfigurationException {

        File eventDirectory = getEventDirectory(event);

        // load dataset if it already exists
        String eventDirName = getLabel(event);
        File dsFile = new File(eventDirectory, eventDirName+".dsml");
        XMLDataSet dataset;
        if (dsFile.exists()) {
            dataset = XMLDataSet.load(dsFile.toURL());
        } else {
            DocumentBuilder docBuilder = XMLDataSet.getDocumentBuilder();
            dataset = new XMLDataSet(docBuilder,
                                     eventDirectory.toURL(),
                                     "genid"+Math.round(Math.random()*Integer.MAX_VALUE),
                                     eventDirName,
                                     System.getProperty("user.name"));

            // add event since dataset is new
            if (event != null) {
                AuditInfo[] audit = new AuditInfo[1];
                audit[0] = new AuditInfo(System.getProperty("user.name"),
                                         "event loaded via sod.");
                dataset.addParameter( StdDataSetParamNames.EVENT, event, audit);
            }
        } // end of else
        return dataset;
    }

    protected String getLabel(EventAccessOperations event) {
        Element labelConfig = SodUtil.getElement(config, "eventDirLabel");
        if (labelConfig == null) {
            String eventFileName =
                regions.getRegionName(event.get_attributes().region);
            try {
                eventFileName+=
                    " "+event.get_preferred_origin().origin_time.date_time;
            } catch (NoPreferredOrigin e) {
                eventFileName+=" "+eventFileNum;
                eventFileNum++;
            } // end of try-catch

            eventFileName = eventFileName.replace(' ', '_');
            eventFileName = eventFileName.replace(',', '_');
            return eventFileName;
        } // end of if (labelConfig == null)

        if (nameGenerator == null) {
            nameGenerator = new NameGenerator(labelConfig);
        }
        return nameGenerator.getName(event);

    }

    DataSet lastDataSet = null;

    int eventFileNum = 1;

    NameGenerator nameGenerator = null;

    ParseRegions regions;

    Element config;

    File dataDirectory;

    static Category logger =
        Category.getInstance(SacFileProcessor.class.getName());

}// SacFileProcessor

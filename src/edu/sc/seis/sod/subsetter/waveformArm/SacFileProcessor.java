
package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.sac.FissuresToSac;
import edu.sc.seis.fissuresUtil.sac.SacTimeSeries;
import edu.sc.seis.fissuresUtil.xml.StdDataSetParamNames;
import edu.sc.seis.fissuresUtil.xml.XMLDataSet;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.LocalSeismogramProcess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.NameGenerator;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
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
    public SacFileProcessor (Element config) {
        this.config = config;
        regions = new ParseRegions();
        String datadirName =
            SodUtil.getText(SodUtil.getElement(config, "dataDirectory"));
        this.dataDirectory = new File(datadirName);
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
        try {
            logger.info("Got "+seismograms.length+" seismograms for "+
                            ChannelIdUtil.toString(channel.get_id())+
                            " for event in "+
                            regions.getRegionName(event.get_attributes().region)+
                            " at "+event.get_preferred_origin().origin_time.date_time);
            if ( ! dataDirectory.exists()) {
                if ( ! dataDirectory.mkdirs()) {
                    throw new ConfigurationException("Unable to create directory."+dataDirectory);
                } // end of if (!)

            } // end of if (dataDirectory.exits())
            String eventDirName = getLabel(event);

            File eventDirectory = getEventDirectory(eventDirName);

            XMLDataSet dataset = getDataSet(eventDirectory, eventDirName, event);

            SacTimeSeries sac;
            String seisFilename = "";
            for (int i=0; i<seismograms.length; i++) {
                seisFilename = ChannelIdUtil.toStringNoDates(seismograms[i].channel_id);
                seisFilename.replace(' ', '.'); // check for space-space site
                File seisFile = new File(eventDirectory, seisFilename);
                int n =0;
                while (seisFile.exists()) {
                    n++;

                    seisFilename =
                        ChannelIdUtil.toStringNoDates(seismograms[i].channel_id)+"."+n;
                    seisFilename.replace(' ', '.'); // check for space-space site
                    seisFile = new File(eventDirectory, seisFilename);
                } // end of while (seisFile.exists())
                LocalSeismogramImpl lseis =
                    (LocalSeismogramImpl)seismograms[i];
                sac = FissuresToSac.getSAC(lseis,
                                           channel,
                                           event.get_preferred_origin());
                sac.write(seisFile);
//                AuditInfo[] audit = new AuditInfo[1];
//                audit[0] = new AuditInfo(System.getProperty("user.name"),
//                                         "seismogram loaded via sod.");
//                dataset.addSeismogramRef(lseis, seisFile.toURL(),
//                                         seisFilename,
//                                         new Property[0],
//                                         lseis.parm_ids,
//                                         audit);
            }

            File outFile = new File(eventDirectory, eventDirName+".dsml");
            OutputStream fos = new BufferedOutputStream(
                new FileOutputStream(outFile));
            dataset.write(fos);
            fos.close();

long mbyte = 1024*1024;
            Runtime runtime = Runtime.getRuntime();
            String s = "Memory usage: "+runtime.freeMemory()/mbyte+" "+
                             runtime.totalMemory()/mbyte+"/"+runtime.maxMemory()/mbyte+" Mb";
            System.out.println(s);

        } catch(Exception e) {
            e.printStackTrace();
            logger.error("EXCEPTION CAUGHT WHILE trying to save dataset", e);
        }
        return seismograms;
    }

    protected File getEventDirectory(String eventDirName)
        throws ConfigurationException {

        File eventDirectory = new File(dataDirectory, eventDirName);
        if ( ! eventDirectory.exists()) {
            if ( ! eventDirectory.mkdirs()) {
                throw new ConfigurationException("Unable to create directory."+eventDirectory);
            } // end of if (!)
        } // end of if (dataDirectory.exits())

        return eventDirectory;
    }

    protected XMLDataSet getDataSet(File eventDirectory,
                                    String eventDirName,
                                    EventAccessOperations event)
        throws MalformedURLException, ParserConfigurationException {
        // load dataset if it already exists
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

    int eventFileNum = 1;

    NameGenerator nameGenerator = null;

    ParseRegions regions;

    Element config;

    File dataDirectory;

    static Category logger =
        Category.getInstance(SacFileProcessor.class.getName());

}// SacFileProcessor

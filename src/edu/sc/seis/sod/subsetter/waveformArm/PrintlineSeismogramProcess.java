package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import org.w3c.dom.*;
import org.apache.log4j.*;

import java.io.*;

/**
 * sample xml file
 *<pre>
 * &lt;sacFileProcessor&gt;
 *        &lt;dataDirectory&gt;SceppEvents&lt;/dataDirectory&gt;
 * &lt;/sacFileProcessor&gt;
 *</pre>
 */

public class PrintlineSeismogramProcess implements LocalSeismogramProcess {
    /**
     * Creates a new <code>PrintlineWaveformProcessor</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public PrintlineSeismogramProcess (Element config){
        filename = SodUtil.getNestedText(config);
        regions = ParseRegions.getInstance();
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
     */
    public LocalSeismogram[] process(EventAccessOperations event,
                                     NetworkAccess network,
                                     Channel channel,
                                     RequestFilter[] original,
                                     RequestFilter[] available,
                                     LocalSeismogram[] seismograms,
                                     CookieJar cookies) throws IOException, NoPreferredOrigin {
        if (filename != null && filename.length() != 0) {
            FileWriter fwriter = new FileWriter(filename, true);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            String debugStr = "Got "+seismograms.length+" seismograms for "+
                ChannelIdUtil.toString(channel.get_id())+
                " for event in "+
                regions.getRegionName(event.get_attributes().region)+
                " at "+event.get_preferred_origin().origin_time.date_time;
            bwriter.write(debugStr, 0, debugStr.length());
            bwriter.newLine();
            bwriter.close();
        } else {
            System.out.println(new java.util.Date()+" "+
                                   edu.sc.seis.fissuresUtil.exceptionHandler.ExceptionReporterUtils.getMemoryUsage()+" "+
                                   " Got "+seismograms.length+" seismograms for "+
                                   ChannelIdUtil.toStringNoDates(channel.get_id())+
                                   " for event in "+
                                   regions.getRegionName(event.get_attributes().region)+
                                   " at "+event.get_preferred_origin().origin_time.date_time);
            logger.debug("Got "+seismograms.length+" seismograms for "+
                             ChannelIdUtil.toStringNoDates(channel.get_id())+
                             " for event in "+
                             regions.getRegionName(event.get_attributes().region)+
                             " at "+event.get_preferred_origin().origin_time.date_time);
        } // end of else

        return seismograms;
    }

    ParseRegions regions;

    String filename = null;

    static private Logger logger =
        Logger.getLogger(PrintlineSeismogramProcess.class);

}// PrintlineWaveformProcessor

package edu.sc.seis.sod.process.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.sod.SodUtil;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

/**
 * sample xml file
 *<pre>
 * &lt;sacFileProcessor&gt;
 *        &lt;dataDirectory&gt;SceppEvents&lt;/dataDirectory&gt;
 * &lt;/sacFileProcessor&gt;
 *</pre>
 */

public class PrintlineSeismogramProcess implements LocalSeismogramProcess {

    public PrintlineSeismogramProcess (Element config){
        filename = SodUtil.getNestedText(config);
        regions = ParseRegions.getInstance();
    }

    public LocalSeismogramImpl[] process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms)
        throws IOException, NoPreferredOrigin {
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

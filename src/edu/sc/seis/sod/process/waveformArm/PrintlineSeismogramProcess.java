package edu.sc.seis.sod.process.waveformArm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.exceptionHandler.ExceptionReporterUtils;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class PrintlineSeismogramProcess implements LocalSeismogramProcess {

    public PrintlineSeismogramProcess(Element config) {
        filename = SodUtil.getNestedText(config);
    }

    public LocalSeismogramResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms,
                                         CookieJar cookieJar)
            throws IOException, NoPreferredOrigin {
        String regionName = regions.getRegionName(event.get_attributes().region);
        String dateTime = event.get_preferred_origin().origin_time.date_time;
        String debugStr = "Got " + seismograms.length + " seismograms for "
                + ChannelIdUtil.toString(channel.get_id()) + " for event in "
                + regionName + " at " + dateTime;
        if(filename != null && filename.length() != 0) {
            FileWriter fwriter = new FileWriter(filename, true);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            bwriter.write(debugStr, 0, debugStr.length());
            bwriter.newLine();
            bwriter.close();
        } else {
            System.out.println(new java.util.Date() + " "
                    + ExceptionReporterUtils.getMemoryUsage() + debugStr);
            logger.debug(debugStr);
        } // end of else
        return new LocalSeismogramResult(true,
                                         seismograms,
                                         new StringTreeLeaf(this, true));
    }

    private ParseRegions regions = ParseRegions.getInstance();

    private String filename;

    private static Logger logger = Logger.getLogger(PrintlineSeismogramProcess.class);
}// PrintlineWaveformProcessor

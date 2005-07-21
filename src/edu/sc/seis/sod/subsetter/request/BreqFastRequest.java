package edu.sc.seis.sod.subsetter.request;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.EventFormatter;

/**
 * BreqFastRequestSubsetter.java Created: Wed Mar 19 14:07:16 2003
 * 
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell </a>
 * @version 1.0
 */
public class BreqFastRequest implements Request {

    public BreqFastRequest(Element config) throws ConfigurationException {
        this.config = config;
        regions = ParseRegions.getInstance();
        String datadirName = getConfig("dataDirectory");
        this.dataDirectory = new File(datadirName);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        nameGenerator = new EventFormatter(SodUtil.getElement(config, "label"),
                                           true);
    }

    public boolean accept(EventAccessOperations event,
                          Channel channel,
                          RequestFilter[] request,
                          CookieJar cookieJar) {
        writeToBFEmail(event, channel, request);
        // don't care if yes or no
        return true;
    }

    protected String getConfig(String name) {
        return SodUtil.getText(SodUtil.getElement(config, name));
    }

    protected synchronized void writeToBFEmail(EventAccessOperations event,
                                               Channel channel,
                                               RequestFilter[] request) {
        if(!dataDirectory.exists()) {
            if(!dataDirectory.mkdirs()) {
                throw new RuntimeException("Unable to create directory."
                        + dataDirectory);
            } // end of if (!)
        } // end of if (dataDirectory.exits())
        File bfFile = new File(dataDirectory, getFileName(event));
        // need to check before the writer is created as it creates the file
        boolean fileExists = bfFile.exists();
        Writer out = null;
        try {
            out = new BufferedWriter(new FileWriter(bfFile.getAbsolutePath(),
                                                    true));
            if(!fileExists) {// first time to this event, insert headers
                insertEventHeader(event, out);
            } // end of if ( ! fileExists)
            for(int i = 0; i < request.length; i++) {
                insertRequest(channel, request, out, i);
            } // end of for (int i=0; i<request.length; i++)
        } catch(IOException e) {
            throw new RuntimeException("Trouble writing breqfast request!", e);
        } finally {
            if(out != null) {
                try {
                    out.close();
                } catch(IOException e) {
                    throw new RuntimeException("Unable to close breqfast file!",
                                               e);
                }
            }
        }
    }

    private void insertRequest(Channel channel,
                               RequestFilter[] request,
                               Writer out,
                               int i) throws IOException {
        MicroSecondDate start = new MicroSecondDate(request[i].start_time);
        MicroSecondDate end = new MicroSecondDate(request[i].end_time);
        out.write(channel.my_site.my_station.get_code() + " "
                + channel.my_site.my_station.my_network.get_code() + " "
                + format.format(start) + tenths.format(start).substring(0, 1)
                + " " + format.format(end) + tenths.format(end).substring(0, 1)
                + " 1 " + channel.get_code() + " " + channel.my_site.get_code()
                + nl);
    }

    private void insertEventHeader(EventAccessOperations event, Writer out)
            throws IOException {
        out.write(".NAME " + getConfig("name") + nl);
        out.write(".INST " + getConfig("inst") + nl);
        out.write(".MAIL " + getConfig("mail") + nl);
        out.write(".EMAIL " + getConfig("email") + nl);
        out.write(".PHONE " + getConfig("phone") + nl);
        out.write(".FAX " + getConfig("fax") + nl);
        out.write(".MEDIA " + getConfig("media") + nl);
        out.write(".ALTERNATE MEDIA " + getConfig("altmedia1") + nl);
        out.write(".ALTERNATE MEDIA " + getConfig("altmedia2") + nl);
        Origin o = EventUtil.extractOrigin(event);
        out.write(".SOURCE " + "~" + o.catalog + " " + o.contributor
                + "~unknown~unknown~" + nl);
        MicroSecondDate oTime = new MicroSecondDate(o.origin_time);
        out.write(".HYPO "
                + "~"
                + format.format(oTime)
                + tenths.format(oTime)
                + "~"
                + o.my_location.latitude
                + "~"
                + o.my_location.longitude
                + "~"
                + ((QuantityImpl)o.my_location.depth).convertTo(UnitImpl.KILOMETER)
                        .getValue() + "~" + "0" + "~"
                + event.get_attributes().region.number + "~"
                + regions.getRegionName(event.get_attributes().region) + "~"
                + nl);
        for(int j = 0; j < o.magnitudes.length; j++) {
            out.write(".MAGNITUDE ~" + o.magnitudes[j].value + "~"
                    + o.magnitudes[j].type + "~" + nl);
        } // end of for (int j=0; j<o.magnitude.length; j++)
        out.write(".QUALITY " + getConfig("quality") + nl);
        out.write(".LABEL " + getLabel(event) + nl);
        out.write(".END" + nl);
        out.write(nl);
    }

    protected String getFileName(EventAccessOperations event) {
        return getLabel(event) + ".breqfast";
    }

    protected String getLabel(EventAccessOperations event) {
        return nameGenerator.getFilizedName(event);
    }

    static final String nl = "\n";

    SimpleDateFormat format = new SimpleDateFormat("yyyy MM dd HH mm ss.");

    SimpleDateFormat tenths = new SimpleDateFormat("SSS");

    ParseRegions regions;

    EventFormatter nameGenerator;

    Element config;

    File dataDirectory;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(BreqFastRequest.class);
} // BreqFastRequestGenerator

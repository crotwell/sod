package edu.sc.seis.sod.subsetter.request;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Instant;

import org.apache.velocity.VelocityContext;
import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.common.ISOTime;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.VelocityFileElementParser;
import edu.sc.seis.sod.util.display.EventUtil;
import edu.sc.seis.sod.util.display.ParseRegions;
import edu.sc.seis.sod.util.display.ThreadSafeSimpleDateFormat;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;
import edu.sc.seis.sod.velocity.WaveformProcessContext;

/**
 * BreqFastRequestSubsetter.java Created: Wed Mar 19 14:07:16 2003
 * 
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell </a>
 * @version 1.0
 */
public class BreqFastRequest implements RequestSubsetter {

    public BreqFastRequest(Element config) throws ConfigurationException {
        this.config = config;
        VelocityFileElementParser parser = new VelocityFileElementParser(config,
                                                                         getDefaultWorkingDir(),
                                                                         getDefaultFileTemplate());
        labelTemplate = DOMHelper.extractText(config,
                                              "label",
                                              "${event.getTime('yyyy.DDD.HH.mm.ss.SSSS')}");
        fullTemplate = parser.getTemplate();
        regions = ParseRegions.getInstance();
    }

    public StringTree accept(CacheEvent event,
                          Channel channel,
                          RequestFilter[] request,
                          CookieJar cookieJar) {
        return new StringTreeLeaf(this, writeToBFEmail(event, channel, request));
    }

    protected String getConfig(String name) {
        return SodUtil.getText(SodUtil.getElement(config, name));
    }

    protected synchronized boolean writeToBFEmail(CacheEvent event,
                                                  Channel channel,
                                                  RequestFilter[] request) {
        VelocityContext ctx = new WaveformProcessContext(event,
                                                         channel,
                                                         request,
                                                         new RequestFilter[0],
                                                         new LocalSeismogramImpl[0],
                                                         null);
        String bfastLoc = FissuresFormatter.filize(velocitizer.evaluate(fullTemplate,
                                                                        ctx));
        File bfFile = new File(bfastLoc);
        File parent = bfFile.getParentFile();
        if(!parent.exists() && !parent.mkdirs()) {
            throw new RuntimeException("Unable to create directory." + parent);
        }
        // need to check before the writer is created as it creates the file
        boolean fileExists = bfFile.exists();
        Writer out = null;
        try {
            out = new BufferedWriter(new FileWriter(bfFile.getAbsolutePath(),
                                                    true));
            if(!fileExists) {// first time to this event, insert headers
                insertEventHeader(event,
                                  out,
                                  velocitizer.evaluate(labelTemplate, ctx));
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
        return true;
    }

    protected void optInsert(Writer out, String configName) throws IOException {
        optInsert(out, configName, configName.toUpperCase());
    }

    protected void optInsert(Writer out, String configName, String fieldName)
            throws IOException {
        if(SodUtil.getElement(config, configName) != null) {
            insert(out, configName, fieldName);
        }
    }

    protected void insert(Writer out, String configName) throws IOException {
        insert(out, configName, configName.toUpperCase());
    }

    protected void insert(Writer out, String configName, String fieldName)
            throws IOException {
        out.write("." + fieldName + " " + getConfig(configName) + nl);
    }

    protected void insertRequest(Channel channel,
                                 RequestFilter[] request,
                                 Writer out,
                                 int i) throws IOException {
        Instant start = request[i].start_time;
        Instant end = request[i].end_time;
        out.write(channel.getStation().getCode() + " "
                + channel.getNetworkCode() + " "
                + format.format(start) + tenths.format(start).substring(0, 1)
                + " " + format.format(end) + tenths.format(end).substring(0, 1)
                + " 1 " + channel.getCode() + " " + channel.getLocCode()
                + nl);
    }

    protected void insertEventHeader(CacheEvent event,
                                     Writer out,
                                     String label) throws IOException {
        insert(out, "name");
        insert(out, "inst");
        insert(out, "mail");
        insert(out, "email");
        insert(out, "phone");
        insert(out, "fax");
        insert(out, "media");
        insert(out, "altmedia1", "ALTERNATIVE MEDIA");
        insert(out, "altmedia2", "ALTERNATIVE MEDIA");
        OriginImpl o = EventUtil.extractOrigin(event);
        out.write(".SOURCE " + "~" + o.getCatalog() + " " + o.getContributor()
                + "~unknown~unknown~" + nl);
        Instant oTime = o.getOriginTime();
        out.write(".HYPO "
                + "~"
                + format.format(oTime)
                + tenths.format(oTime)
                + "~"
                + o.getLocation().latitude
                + "~"
                + o.getLocation().longitude
                + "~"
                + ((QuantityImpl)o.getLocation().depth).convertTo(UnitImpl.KILOMETER)
                        .getValue() + "~" + "0" + "~"
                + event.get_attributes().region.number + "~"
                + regions.getRegionName(event.get_attributes().region) + "~"
                + nl);
        for(int j = 0; j < o.getMagnitudes().length; j++) {
            out.write(".MAGNITUDE ~" + o.getMagnitudes()[j].value + "~"
                    + o.getMagnitudes()[j].type + "~" + nl);
        } // end of for (int j=0; j<o.magnitude.length; j++)
        insert(out, "quality");
        out.write(".LABEL " + label + nl);
        out.write(".END" + nl);
        out.write(nl);
    }

    protected String getDefaultWorkingDir() {
        return "breqfast";
    }

    protected String getDefaultFileTemplate() {
        return "${event.getTime('yyyy.DDD.HH.mm.ss.SSSS')}.breq";
    }

    static final String nl = "\n";

    ThreadSafeSimpleDateFormat format = new ThreadSafeSimpleDateFormat("yyyy MM dd HH mm ss.", ISOTime.UTC);

    ThreadSafeSimpleDateFormat tenths = new ThreadSafeSimpleDateFormat("SSS", ISOTime.UTC);

    ParseRegions regions;

    Element config;

    private String fullTemplate, labelTemplate;

    private SimpleVelocitizer velocitizer = new SimpleVelocitizer();
} // BreqFastRequestGenerator

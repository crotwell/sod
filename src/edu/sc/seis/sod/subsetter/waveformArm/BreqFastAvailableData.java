package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;
import org.apache.log4j.*;
import java.io.*;
import java.text.*;

/**
 * Creates a breqfast email requset file based on the events and channels that
 * sod finds. This is done as a AvailableDataSubsetter because the data may
 * not be available via a DHI server. See also BreqfastRequestSubsetter if
 * calling available_data is not required, as the speed will be greatly
 * improved.
 *
 * Created: Fri Oct 11 15:40:03 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class BreqFastAvailableData  implements AvailableDataSubsetter, SodElement {
    public BreqFastAvailableData(Element config) {
        this.config = config;
        regions = new ParseRegions();
        String datadirName = getConfig("dataDirectory");
        this.dataDirectory = new File(datadirName);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public boolean accept(EventAccessOperations event,
                          NetworkAccess network,
                          Channel channel,
                          RequestFilter[] request,
                          RequestFilter[] available,
                          CookieJar cookies) throws IOException, ConfigurationException {
        writeToBFEmail(event, network, channel, request, cookies);
        // don't care if yes or no
        return true;
    }

    protected String getConfig(String name) {
        return SodUtil.getText(SodUtil.getElement(config, name));
    }

    protected synchronized void writeToBFEmail(EventAccessOperations event,
                                               NetworkAccess networkAccess,
                                               Channel channel,
                                               RequestFilter[] request,
                                               CookieJar cookies)
        throws IOException, ConfigurationException, NoPreferredOrigin {

        if ( out != null && event != lastEvent) {
            out.close();
            out = null;
            lastEvent = event;
        }

        if (out == null ) {
            logger.debug("opening file");
            if ( ! dataDirectory.exists()) {
                if ( ! dataDirectory.mkdirs()) {
                    throw new ConfigurationException("Unable to create directory."+dataDirectory);
                } // end of if (!)

            } // end of if (dataDirectory.exits())

            File bfFile = new File(dataDirectory, getFileName(event));
            if (bfFile.exists()) {
                fileExists = true;
            } else {
                fileExists = false;
            } // end of else
            out = new BufferedWriter(new FileWriter(bfFile.getAbsolutePath(),
                                                    true));
        }

        if ( ! fileExists) {
            fileExists = true;
            // first tme to this event, insert headers
            out.write(".NAME "+getConfig("name")+nl);
            out.write(".INST "+getConfig("inst")+nl);
            out.write(".MAIL "+getConfig("mail")+nl);
            out.write(".EMAIL "+getConfig("email")+nl);
            out.write(".PHONE "+getConfig("phone")+nl);
            out.write(".FAX "+getConfig("fax")+nl);
            out.write(".MEDIA "+getConfig("media")+nl);
            out.write(".ALTERNATE MEDIA "+getConfig("altmedia1")+nl);
            out.write(".ALTERNATE MEDIA "+getConfig("altmedia2")+nl);
            Origin o = event.get_preferred_origin();
            out.write(".SOURCE "
                          +"~"+o.catalog
                          +" "+o.contributor
                          +"~unknown~unknown~"+nl);
            MicroSecondDate oTime = new MicroSecondDate(o.origin_time);
            out.write(".HYPO "
                          +"~"+format.format(oTime)
                          +tenths.format(oTime)
                          +"~"
                          +o.my_location.latitude
                          +"~"
                          +o.my_location.longitude
                          +"~"
                          +((QuantityImpl)o.my_location.depth).convertTo(UnitImpl.KILOMETER).getValue()
                          +"~"
                          +"0"
                          +"~"
                          +event.get_attributes().region.number
                          +"~"
                          +regions.getRegionName(event.get_attributes().region)
                          +"~"
                          +nl);
            for (int j=0; j<o.magnitudes.length; j++) {
                out.write(".MAGNITUDE ~"+o.magnitudes[j].type+"~"+o.magnitudes[j].value+"~"+nl);
            } // end of for (int j=0; j<o.magnitude.length; j++)


            out.write(".QUALITY "+getConfig("quality")+nl);
            out.write(".LABEL "+getLabel(event)+nl);
            out.write(".END"+nl);
            out.write(nl);
        } // end of if ( ! fileExists)

        MicroSecondDate start, end;
        for (int i=0; i<request.length; i++) {
            start = new MicroSecondDate(request[i].start_time);
            end = new MicroSecondDate(request[i].end_time);

            out.write(channel.my_site.my_station.get_code()
                          +" "+
                          channel.my_site.my_station.my_network.get_code()
                          +" "+
                          format.format(start)+tenths.format(start).substring(0,1)
                          +" "+
                          format.format(end)+tenths.format(end).substring(0,1)
                          +" 1 "+
                          channel.get_code()
                          +" "+
                          channel.my_site.get_code()
                          +nl);

        } // end of for (int i=0; i<request.length; i++)
    }

    protected String getFileName(EventAccessOperations event) {
        return getLabel(event)+".breqfast";
    }

    protected String getLabel(EventAccessOperations event) {
        if (nameGenerator == null) {
            nameGenerator = new EventFormatter(SodUtil.getElement(config,
                                                                  "label"));
        }
        return nameGenerator.getFilizedName(event);
    }

    public void finalize() {
        if (out != null) {
            try {
                out.flush();
                out.close();
                out = null;
            } catch (IOException e) {
                // oh well...
            } // end of try-catch

        } // end of if ()
    }

    static final String nl = "\n";

    SimpleDateFormat format = new SimpleDateFormat("yyyy MM dd HH mm ss.");
    SimpleDateFormat tenths = new SimpleDateFormat("SSS");

    ParseRegions regions;
    EventFormatter nameGenerator = null;

    Element config;

    File dataDirectory;
    boolean fileExists;

    BufferedWriter out = null;
    EventAccessOperations lastEvent = null;

    private static Category logger =
        Category.getInstance(BreqFastAvailableData.class.getName());

}// BreqFastEventChannelSubsetter

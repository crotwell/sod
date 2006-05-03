package edu.sc.seis.sod.subsetter.origin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.w3c.dom.Element;
import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.event.CSVEventSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author oliverpa
 * 
 * Created on Jul 5, 2005
 */
public class CSVEventPrinter implements OriginSubsetter {

    public CSVEventPrinter(Element config) throws Exception {
        Element filenameEl = SodUtil.getElement(config, "filename");
        String filename = SodUtil.getNestedText(filenameEl);
        file = new File(filename);
        createFileAndWriteHeaderIfNeeded();
    }

    public StringTree accept(EventAccessOperations eventAccess,
                             EventAttr eventAttr,
                             Origin preferred_origin) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
        writer.write(preferred_origin.origin_time.date_time + COM);
        Location loc = preferred_origin.my_location;
        writer.write(loc.latitude + COM);
        writer.write(loc.longitude + COM);
        Quantity q = loc.depth;
        writer.write(q.value + COM);
        writer.write(q.the_units + COM);
        Magnitude mag = preferred_origin.magnitudes[0];
        writer.write(mag.value + COM);
        writer.write(mag.type + COM);
        writer.write(preferred_origin.catalog + COM);
        writer.write(preferred_origin.contributor + COM);
        writer.write(eventAttr.name + COM);
        FlinnEngdahlRegion region = eventAttr.region;
        writer.write(region.number + COM);
        writer.write("" + region.type.value());
        writer.newLine();
        writer.close();
        return new StringTreeLeaf(this, true);
    }

    private void createFileAndWriteHeaderIfNeeded() throws IOException {
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(getHeader());
            writer.newLine();
            writer.close();
        }
    }

    private String getHeader() {
        return CSVEventSource.TIME + COM + CSVEventSource.LATITUDE + COM
                + CSVEventSource.LONGITUDE + COM + CSVEventSource.DEPTH + COM
                + CSVEventSource.DEPTH_UNITS + COM + CSVEventSource.MAGNITUDE
                + COM + CSVEventSource.MAGNITUDE_TYPE + COM
                + CSVEventSource.CATALOG + COM + CSVEventSource.CONTRIBUTOR
                + COM + CSVEventSource.NAME + COM + CSVEventSource.FE_REGION
                + COM + CSVEventSource.FE_REGION_TYPE;
    }

    private File file;

    private static final String COM = ", ";
}
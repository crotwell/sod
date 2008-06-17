package edu.sc.seis.sod.subsetter.origin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.w3c.dom.Element;
import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
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
        this(DOMHelper.extractText(config, "filename", STDOUT));
    }

    public CSVEventPrinter(String filename) throws Exception {
        if(!filename.equals(STDOUT)) {
            file = new File(filename);
        }
        createFileAndWriteHeaderIfNeeded();
    }

    public StringTree accept(CacheEvent eventAccess,
                             EventAttr eventAttr,
                             Origin preferred_origin) throws Exception {
        StringBuffer buff = new StringBuffer();
        buff.append(preferred_origin.getOriginTime().date_time + COM);
        Location loc = preferred_origin.getLocation();
        buff.append(loc.latitude + COM);
        buff.append(loc.longitude + COM);
        Quantity q = loc.depth;
        buff.append(q.value + COM);
        buff.append(q.the_units + COM);
        StringBuffer magValBuf = new StringBuffer();
        StringBuffer magTypeBuf = new StringBuffer();
        StringBuffer magContribBuf = new StringBuffer();
        Magnitude[] mags = preferred_origin.getMagnitudes();
        for(int i = 0; i < mags.length; i++) {
            magValBuf.append(mags[i].value);
            if(mags[i].type == null || mags[i].type.equals("")) {
                magTypeBuf.append("");
            } else {
                magTypeBuf.append(mags[i].type);
            }
            if(mags[i].contributor == null || mags[i].contributor.equals("")) {
                magContribBuf.append("");
            } else {
                magContribBuf.append(mags[i].contributor);
            }
            if(i < mags.length - 1) {
                magValBuf.append(':');
                magTypeBuf.append(':');
                magContribBuf.append(':');
            }
        }
        buff.append(magValBuf + COM);
        buff.append(magTypeBuf + COM);
        buff.append(magContribBuf + COM);
        buff.append(preferred_origin.getCatalog() + COM);
        buff.append(preferred_origin.getContributor() + COM);
        buff.append(eventAttr.name + COM);
        FlinnEngdahlRegion region = eventAttr.region;
        buff.append(region.number + COM);
        buff.append("" + region.type.value());
        if(file == null) {
            System.out.println(buff.toString());
        } else {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file,
                                                                      true));
            writer.write(buff.toString());
            writer.newLine();
            writer.close();
        }
        return new StringTreeLeaf(this, true);
    }

    private void createFileAndWriteHeaderIfNeeded() throws IOException {
        if(file == null) {
            System.out.println(getHeader());
        } else if(!file.exists()) {
            file.getAbsoluteFile().getParentFile().mkdirs();
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
                + CSVEventSource.MAGNITUDE_CONTRIBUTOR + COM
                + CSVEventSource.CATALOG + COM + CSVEventSource.CONTRIBUTOR
                + COM + CSVEventSource.NAME + COM + CSVEventSource.FE_REGION
                + COM + CSVEventSource.FE_REGION_TYPE;
    }

    private File file;

    private static final String COM = ", ";

    private static final String STDOUT = "<stdout>";
}
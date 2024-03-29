package edu.sc.seis.sod.velocity.event;

import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;
import java.util.TimeZone;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.FlinnEngdahlType;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.ProxyEventAccessOperations;
import edu.sc.seis.fissuresUtil.chooser.ThreadSafeDecimalFormat;
import edu.sc.seis.fissuresUtil.chooser.ThreadSafeSimpleDateFormat;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.xml.XMLEvent;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;
import edu.sc.seis.sod.velocity.network.VelocityStation;

/**
 * @author groves Created on Dec 14, 2004
 */
public class VelocityEvent extends ProxyEventAccessOperations {

    public VelocityEvent(CacheEvent event) {
        if (event == null) {throw new NullPointerException("event cannot be null");}
        this.event = event;
        this.origin = getOrigin();
    }

    public String getName() {
        if (get_attributes().name != null & get_attributes().name.length() > 0) {
            return get_attributes().name;
        } else if (hasRegion()) {
            return getRegion();
        } else {
            return getTime();
        }
    }

    public boolean hasRegion() {
        return get_attributes().region != null && get_attributes().region.number > 0;
    }

    public String getRegion() {
        return pr.getRegionName(get_attributes().region);
    }

    public String getRegionNumber(){
        FlinnEngdahlRegion region = get_attributes().region;
        String type = region.type == FlinnEngdahlType.GEOGRAPHIC_REGION ? "Geographic" : "Seismic";
        return type + " " + get_attributes().region.number;
    }

    public String getMagnitude() {
        if(origin.getMagnitudes().length == 0) {
            return "-";
        }
        return FissuresFormatter.formatMagnitude(origin.getMagnitudes()[0]);
    }

    public String getAllMagnitudes() {
        return getAllMagnitudes(", ");
    }

    public String getAllMagnitudes(String seperator) {
        String s = "";
        for(int i = 0; i < origin.getMagnitudes().length; i++) {
            if (s.length() != 0) {s += seperator;}
            s += FissuresFormatter.formatMagnitude(origin.getMagnitudes()[i]);
        }
        for (int j = 0; j < getOrigins().length; j++) {
            OriginImpl o = (OriginImpl)event.get_origins()[j];
            for(int i = 0; i < o.getMagnitudes().length; i++) {
                if (s.length() != 0) {s += seperator;}
                s += FissuresFormatter.formatMagnitude(o.getMagnitudes()[i]);
            }
        }
        return s;
    }

    public String getMagnitudeValue() {
        if (origin.getMagnitudes().length != 0) {
        return "" + origin.getMagnitudes()[0].value;
        } else {
            return "";
        }
    }

    public String getMagnitudeType() {
        if (origin.getMagnitudes().length != 0) {
            return origin.getMagnitudes()[0].type;
        } else {
            return "";
        }
    }

    public String getMagnitudeContributor() {
        if (origin.getMagnitudes().length != 0) {
            return origin.getMagnitudes()[0].contributor;
        } else {
            return "";
        }
    }

    public String getLatitude() {
        return df.format(origin.getLocation().latitude);
    }

    public String getLongitude() {
        return df.format(origin.getLocation().longitude);
    }

    public String getLatitude(String format) {
        return new DecimalFormat(format).format(origin.getLocation().latitude);
    }

    public String getLongitude(String format) {
        return new DecimalFormat(format).format(origin.getLocation().longitude);
    }


    public Float getFloatLatitude() {
        return new Float(origin.getLocation().latitude);
    }

    public Float getFloatLongitude() {
        return new Float(origin.getLocation().longitude);
    }

    public String getOrientedLatitude() {
        if(origin.getLocation().latitude < 0) {
            return df.format(-origin.getLocation().latitude) + " S";
        }
        return df.format(origin.getLocation().latitude) + " N";
    }

    public String getOrientedLongitude() {
        if(origin.getLocation().longitude < 0) {
            return df.format(-origin.getLocation().longitude) + " W";
        }
        return df.format(origin.getLocation().longitude) + " E";
    }

    public String getDepth() {
        return FissuresFormatter.formatDepth(FissuresFormatter.getDepth(origin));
    }

    public String getDepthValue() {
        return getDepth("0.0");
    }

    public String getDepth(String format) {
        double depthInKM = QuantityImpl.createQuantityImpl(origin.getLocation().depth)
                .convertTo(UnitImpl.KILOMETER).value;
        return new DecimalFormat(format).format(depthInKM);
    }

    public String getElevation() {
        return FissuresFormatter.formatDepth(QuantityImpl.createQuantityImpl(origin.getLocation().elevation));
    }

    public String getElevation(String format) {
        double elevInMeters = QuantityImpl.createQuantityImpl(origin.getLocation().elevation)
                .convertTo(UnitImpl.METER).value;
        return new DecimalFormat(format).format(elevInMeters);
    }

    public String getTime() {
        return getTime("yyyy/MM/dd HH:mm:ss z");
    }

    public String getTimePrecise() {
        return getTime("yyyy/MM/dd HH:mm:ss.SSS Z");
    }

    public String getFilizedTime() {
        return getTime("yyyy_DDD_HH_mm_ss_ZZZ");
    }

    public String getTime(String format) {
        return SimpleVelocitizer.format(new MicroSecondDate(origin.getOriginTime()),
                                        format);
    }

    public String getCatalog() {
        return origin.getCatalog();
    }

    public String getContributor() {
        return origin.getContributor();
    }

    public int getId() {
        return getDbid();
    }

    /** just because I can never remember if it is i or I */
    public int getDbId() {
        return getDbid();
    }

    public int getDbid() {
        return ((CacheEvent)event).getDbid();
    }

    public OriginImpl getPreferred() throws NoPreferredOrigin {
        return getCacheEvent().getPreferred();
    }

    public Origin[] getOrigins() {
        return getCacheEvent().getOrigins();
    }

    public String getParam(String name) {
        for(int i = 0; i < origin.getParmIds().length; i++) {
            if(origin.getParmIds()[i].a_id.equals(name)) {
                return origin.getParmIds()[i].creator;
            }
        }
        return null;
    }

    public String getDistanceDeg(VelocityStation sta) {
        return sta.getDistanceDeg(this);
    }

    public String getDistance(VelocityStation sta) {
        return sta.getDistance(this);
    }

    public String getAz(VelocityStation sta) {
        return sta.getAz(this);
    }

    public String getBaz(VelocityStation sta) {
        return sta.getBaz(this);
    }

    public void setPosition(int[] position) {
        this.position = position;
    }

    public int[] getPosition() {
        return position;
    }

    public String toXML() throws XMLStreamException {
        StringWriter writer = new StringWriter();
        XMLStreamWriter xmlWriter = XMLUtil.getStaxOutputFactory().createXMLStreamWriter(writer);
        XMLEvent.insert(xmlWriter, this);
        return writer.toString();
    }

    public String getURL() {
        return "earthquakes/" + makeDateIdentifier(this);
    }

    public CacheEvent getCacheEvent() {
        if(event instanceof CacheEvent) {
            return (CacheEvent)event;
        }
        return new CacheEvent(event);
    }

    private static ThreadSafeSimpleDateFormat fullDateIdentifier = new ThreadSafeSimpleDateFormat("yyyy/MM/dd/HH/mm/ss", ISOTime.UTC);

    public static String makeDateIdentifier(VelocityEvent event) {
        synchronized(fullDateIdentifier) {
        return fullDateIdentifier.format(new MicroSecondDate(event.getOrigin().getOriginTime()));
        }
    }

    public static MicroSecondDate parseDateIdentifier(String eqIdentifier)
            throws ParseException {
        synchronized(fullDateIdentifier) {
        return new MicroSecondDate(fullDateIdentifier.parse(eqIdentifier));
        }
    }

    private Origin origin;

    private int[] position;

    private static ParseRegions pr = ParseRegions.getInstance();

    private ThreadSafeDecimalFormat df = new ThreadSafeDecimalFormat("0.0");

    public static VelocityEvent[] wrap(List evs) {
        VelocityEvent[] velEvs = new VelocityEvent[evs.size()];
        for(int i = 0; i < velEvs.length; i++) {
            velEvs[i] = wrap((EventAccessOperations)evs.get(i));
        }
        return velEvs;
    }

    public static VelocityEvent[] wrap(EventAccessOperations[] evs) {
        VelocityEvent[] velEvs = new VelocityEvent[evs.length];
        for(int i = 0; i < velEvs.length; i++) {
            velEvs[i] = wrap(evs[i]);
        }
        return velEvs;
    }

    public static VelocityEvent wrap(EventAccessOperations event) {
        if(event instanceof VelocityEvent) {
            return (VelocityEvent)event;
        } else if(event instanceof CacheEvent) {
            return new VelocityEvent((CacheEvent)event);
        } else {
            return new VelocityEvent(new CacheEvent(event));
        }
    }

// needed to compile under java11? 
    public org.omg.CORBA.InterfaceDef _get_interface() {
      throw new RuntimeException("should never be called");
    }
    public org.omg.CORBA.Object _get_component() {
      throw new RuntimeException("should never be called");
    }
}

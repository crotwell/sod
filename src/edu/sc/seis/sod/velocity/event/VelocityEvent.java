package edu.sc.seis.sod.velocity.event;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.FlinnEngdahlType;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.ProxyEventAccessOperations;
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
        this.event = event;
        this.origin = getOrigin();
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
        if(origin.magnitudes.length == 0) {
            return "-";
        }
        return FissuresFormatter.formatMagnitude(origin.magnitudes[0]);
    }

    public String getAllMagnitudes() {
        return getAllMagnitudes(", ");
    }

    public String getAllMagnitudes(String seperator) {
        String s = "";
        for(int i = 0; i < origin.magnitudes.length; i++) {
            s += FissuresFormatter.formatMagnitude(origin.magnitudes[i]);
            if(i != origin.magnitudes.length - 1) {
                s += seperator;
            }
        }
        return s;
    }

    public String getMagnitudeValue() {
        return "" + origin.magnitudes[0].value;
    }

    public String getMagnitudeType() {
        return origin.magnitudes[0].type;
    }

    public String getLatitude() {
        return df.format(origin.my_location.latitude);
    }

    public String getLongitude() {
        return df.format(origin.my_location.longitude);
    }

    public String getLatitude(String format) {
        return new DecimalFormat(format).format(origin.my_location.latitude);
    }

    public String getLongitude(String format) {
        return new DecimalFormat(format).format(origin.my_location.longitude);
    }


    public Float getFloatLatitude() {
        return new Float(origin.my_location.latitude);
    }

    public Float getFloatLongitude() {
        return new Float(origin.my_location.longitude);
    }

    public String getOrientedLatitude() {
        if(origin.my_location.latitude < 0) {
            return df.format(-origin.my_location.latitude) + " S";
        }
        return df.format(origin.my_location.latitude) + " N";
    }

    public String getOrientedLongitude() {
        if(origin.my_location.longitude < 0) {
            return df.format(-origin.my_location.longitude) + " W";
        }
        return df.format(origin.my_location.longitude) + " E";
    }

    public String getDepth() {
        return FissuresFormatter.formatDepth(FissuresFormatter.getDepth(origin));
    }
    
    public String getDepthValue() {
        return df.format(FissuresFormatter.getDepth(origin).getValue(UnitImpl.KILOMETER));
    }

    public String getDepth(String format) {
        double depthInKM = QuantityImpl.createQuantityImpl(origin.my_location.depth)
                .convertTo(UnitImpl.KILOMETER).value;
        return new DecimalFormat(format).format(depthInKM);
    }

    public String getElevation() {
        return FissuresFormatter.formatDepth(QuantityImpl.createQuantityImpl(origin.my_location.elevation));
    }

    public String getElevation(String format) {
        double elevInMeters = QuantityImpl.createQuantityImpl(origin.my_location.elevation)
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
        return SimpleVelocitizer.format(new MicroSecondDate(origin.origin_time),
                                        format);
    }

    public String getCatalog() {
        return origin.catalog;
    }

    public String getContributor() {
        return origin.contributor;
    }

    public int getId() {
        return ((CacheEvent)event).getDbId();
    }

    public int getDbId() {
        return ((CacheEvent)event).getDbId();
    }

    public String getParam(String name) {
        for(int i = 0; i < origin.parm_ids.length; i++) {
            if(origin.parm_ids[i].a_id.equals(name)) {
                return origin.parm_ids[i].creator;
            }
        }
        return null;
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
        XMLStreamWriter xmlWriter = XMLUtil.staxOutputFactory.createXMLStreamWriter(writer);
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

    private static DateFormat fullDateIdentifier = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
    static {
        fullDateIdentifier.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static String makeDateIdentifier(VelocityEvent event) {
        return fullDateIdentifier.format(new MicroSecondDate(event.getOrigin().origin_time));
    }

    public static MicroSecondDate parseDateIdentifier(String eqIdentifier)
            throws ParseException {
        return new MicroSecondDate(fullDateIdentifier.parse(eqIdentifier));
    }

    private Origin origin;

    private int[] position;

    private static ParseRegions pr = ParseRegions.getInstance();

    private DecimalFormat df = new DecimalFormat("0.0");

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
}
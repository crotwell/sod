package edu.sc.seis.sod.velocity.event;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.ProxyEventAccessOperations;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.sod.status.FissuresFormatter;
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

    public String getMagnitude() {
        if(origin.magnitudes.length == 0) {
            return "-";
        }
        return FissuresFormatter.formatMagnitude(origin.magnitudes[0]);
    }

    public String getAllMagnitudes() {
        String s = "";
        for(int i = 0; i < origin.magnitudes.length; i++) {
            s += FissuresFormatter.formatMagnitude(origin.magnitudes[i]);
            if(i != origin.magnitudes.length - 1) {
                s += ", ";
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
        DateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(new MicroSecondDate(origin.origin_time));
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

    public String getURL() {
        return "earthquakes/" + makeDateIdentifier(this);
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

    public static VelocityEvent[] wrap(CacheEvent[] evs) {
        VelocityEvent[] velEvs = new VelocityEvent[evs.length];
        for(int i = 0; i < velEvs.length; i++) {
            velEvs[i] = new VelocityEvent(evs[i]);
        }
        return velEvs;
    }
}
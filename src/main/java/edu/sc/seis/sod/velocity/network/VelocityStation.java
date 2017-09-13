package edu.sc.seis.sod.velocity.network;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.common.UnknownUnit;
import edu.sc.seis.sod.model.station.StationId;
import edu.sc.seis.sod.model.station.StationIdUtil;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.util.convert.stationxml.StationXMLToFissures;
import edu.sc.seis.sod.util.display.ThreadSafeDecimalFormat;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;
import edu.sc.seis.sod.velocity.event.VelocityEvent;

/**
 * @author groves Created on Jan 7, 2005
 */
public class VelocityStation  {

    public VelocityStation(Station sta) {
        if (sta == null) {
            throw new IllegalArgumentException("StationImpl cannot be null");
        }
        this.sta = sta;
    }

    public Integer getDbId() {
        return sta.getDbid();
    }

    public StationId get_id() {
        return StationId.of(sta);
    }
    
    public StationId getId() {
        return StationId.of(sta);
    }

    public String getCode() {
        return sta.getCode();
    }

    public String getCodes() {
        return getNetCode() + "." + getCode();
    }

    public String getNetCode() {
        return getNet().getCode();
    }

    public VelocityNetwork getNet() {
        if(velocityNet == null) {
            velocityNet = new VelocityNetwork(sta.getNetwork());
        }
        return velocityNet;
    }

    public Instant getStartDateTime() {
        return sta.getStartDateTime();
    }

    public Instant getEndDateTime() {
        return sta.getEndDateTime();
    }

    public String getStart() {
        return FissuresFormatter.formatDate(getStartDateTime());
    }

    public String getStart(String dateFormat) {
        if(dateFormat.equals("longfile")) {
            return FissuresFormatter.formatDateForFile(getStartDateTime());
        }
        return SimpleVelocitizer.format(getStartDateTime(),
                                        dateFormat);
    }

    public String getEnd() {
        return FissuresFormatter.formatDate(getEndDateTime());
    }

    public String getEnd(String dateFormat) {
        if(dateFormat.equals("longfile")) {
            return FissuresFormatter.formatDateForFile(getEndDateTime());
        }
        return SimpleVelocitizer.format(getEndDateTime(),
                                        dateFormat);
    }

    public String getName() {
        return FissuresFormatter.oneLineAndClean(sta.getName());
    }

    public String getCSVName() {
        return getName().replaceAll(",", "");
    }

    public String getDescription() {
        return sta.getDescription();
    }

    public String getOperator() {
        return sta.getOperatorList().get(0).getAgencyList().get(0);
    }

    public String getComment() {
        return sta.getCommentList().get(0).getValue();
    }

    public String getLatitude() {
        return df.format(sta.getLatitude());
    }

    public String getLatitude(String format) {
        return new DecimalFormat(format).format(sta.getLatitude());
    }

    public String getLongitude() {
        return df.format(sta.getLongitude());
    }

    public String getLongitude(String format) {
        return new DecimalFormat(format).format(sta.getLongitude());
    }


    public String getOrientedLatitude() {
        return getOrientedLatitude(sta.getLatitude().getValue());
    }
    
    public static String getOrientedLatitude(float latitude) {
        if(latitude < 0) {
            return df.format(-latitude) + " S";
        }
        return df.format(latitude) + " N";
    }

    public String getOrientedLongitude() {
        return getOrientedLongitude(sta.getLongitude().getValue());
    }

    public static String getOrientedLongitude(float longitude) {
        if(longitude < 0) {
            return df.format(-longitude) + " W";
        }
        return df.format(longitude) + " E";
    }

    public Float getFloatLatitude() {
        return new Float(sta.getLatitude().getValue());
    }

    public Float getFloatLongitude() {
        return new Float(sta.getLongitude().getValue());
    }

    public String getElevation() throws UnknownUnit {
        return FissuresFormatter.formatElevation(StationXMLToFissures.convertFloatType(sta.getElevation()));
    }

    public String getElevation(String format) throws UnknownUnit {
        double elevInMeters = StationXMLToFissures.convertFloatType(sta.getElevation())
                .convertTo(UnitImpl.METER).getValue();
        return new DecimalFormat(format).format(elevInMeters);
    }

    public String getDistance(VelocityEvent event) {
        double km = DistAz.degreesToKilometers(new DistAz(sta, event).getDelta());
        return FissuresFormatter.formatDistance(new QuantityImpl(km,
                                                                 UnitImpl.KILOMETER));
    }

    public String getDistanceDeg(VelocityEvent event) {
        return FissuresFormatter.formatDistance(getDist(event));
    }

    public String getAz(VelocityEvent event) {
        double az = new DistAz(sta, event).getAz();
        return FissuresFormatter.formatQuantity(new QuantityImpl(az,
                                                                 UnitImpl.DEGREE));
    }

    public QuantityImpl getDist(VelocityEvent event) {
        double deg = new DistAz(sta, event).getDelta();
        return new QuantityImpl(deg, UnitImpl.DEGREE);
    }

    public String getBaz(VelocityEvent event) {
        double baz = new DistAz(sta, event).getBaz();
        return FissuresFormatter.formatQuantity(new QuantityImpl(baz,
                                                                 UnitImpl.DEGREE));
    }

    public String getURL() {
        return "stations/" + getNetCode() + "/" + getCode();
    }

    public String toString() {
        return StationIdUtil.toStringNoDates(get_id());
    }

    public boolean equals(Object o) {
        if(o == this) {
            return true;
        }
        if(o instanceof VelocityStation) {
            VelocityStation oVel = (VelocityStation)o;
            if(oVel.getDbId() != -1 && getDbId() != -1 && oVel.getDbId() == getDbId()) {
                return true;
            }
        }
        if(o instanceof Station) {
            Station oSta = (Station)o;
            return StationIdUtil.areEqual(oSta, sta);
        }
        return false;
    }

    public int hashCode() {
        if(!hashCalc) {
            hash = StationIdUtil.toString(get_id()).hashCode();
            hashCalc = true;
        }
        return hash;
    }

    private boolean hashCalc = false;

    private int hash = 0;

    private VelocityNetwork velocityNet = null;

    private Station sta;
    
    private int[] position;

    public void setPosition(int[] position) {
        this.position = position;
    }

    public int[] getPosition() {
        return position;
    }

    static final ThreadSafeDecimalFormat df = new ThreadSafeDecimalFormat("0.00");

    public void insertIntoContext(VelocityContext ctx) {
        ctx.put("station", this);
        ctx.put("sta", this);
        getNet().insertIntoContext(ctx);
    }
    
    public Station getWrapped() {
        return sta;
    }

    public static VelocityStation[] wrap(Station[] stations) {
        VelocityStation[] out = new VelocityStation[stations.length];
        for(int i = 0; i < out.length; i++) {
            out[i] = new VelocityStation((Station)stations[i]);
        }
        return out;
    }
    
    public static List<VelocityStation> wrapList(List<? extends Station> stations) {
        List<VelocityStation> out = new ArrayList<VelocityStation>();
        for(Station s : stations) {
            out.add(new VelocityStation((Station)s));
        }
        return out;
    }
    
}
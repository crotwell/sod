package edu.sc.seis.sod.velocity.network;

import org.apache.velocity.VelocityContext;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.station.SiteId;
import edu.sc.seis.sod.model.station.SiteIdUtil;
import edu.sc.seis.sod.model.station.SiteImpl;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

/**
 * @author groves Created on Jan 7, 2005
 */
public class VelocitySite extends SiteImpl {

    public VelocitySite(SiteImpl s) {
        this.site = s;
        my_location = s.getLocation();
        effective_time = s.getEffectiveTime();
        setStation(s.getStation());
        comment = s.getComment();
    }

    public SiteId get_id() {
        return site.get_id();
    }

    public String get_code() {
        return site.get_code();
    }

    public String getCode() {
        return get_code();
    }

    public String getCodes() {
        return getNet().getCode() + "." + getStation().getCode() + "."
                + getCode();
    }

    public VelocityStation getStation() {
        return new VelocityStation((Station)site.getStation());
    }

    public VelocityNetwork getNet() {
        return getStation().getNet();
    }

    public MicroSecondDate getStartDate() {
        return new MicroSecondDate(effective_time.getBeginTime());
    }

    public MicroSecondDate getEndDate() {
        return new MicroSecondDate(effective_time.getEndTime());
    }

    public String getStart() {
        return FissuresFormatter.formatDate(effective_time.getBeginTime());
    }

    public String getStart(String dateFormat) {
        if(dateFormat.equals("longfile")) {
            return FissuresFormatter.formatDateForFile(effective_time.getBeginTime());
        }
        return SimpleVelocitizer.format(new MicroSecondDate(effective_time.getBeginTime()),
                                        dateFormat);
    }

    public String getEnd() {
        return FissuresFormatter.formatDate(effective_time.getEndTime());
    }

    public String getEnd(String dateFormat) {
        if(dateFormat.equals("longfile")) {
            return FissuresFormatter.formatDateForFile(effective_time.getEndTime());
        }
        return SimpleVelocitizer.format(new MicroSecondDate(effective_time.getEndTime()),
                                        dateFormat);
    }

    public String getComment() {
        return comment;
    }

    public String getLatitude() {
        return VelocityStation.df.format(site.getLocation().latitude);
    }

    public String getLongitude() {
        return VelocityStation.df.format(site.getLocation().longitude);
    }

    public String getOrientedLatitude() {
        if(site.getLocation().latitude < 0) {
            return VelocityStation.df.format(-site.getLocation().latitude) + " S";
        }
        return VelocityStation.df.format(site.getLocation().latitude) + " N";
    }

    public String getOrientedLongitude() {
        if(site.getLocation().longitude < 0) {
            return VelocityStation.df.format(-site.getLocation().longitude)
                    + " W";
        }
        return VelocityStation.df.format(site.getLocation().longitude) + " E";
    }

    public String getDepth() {
        return FissuresFormatter.formatElevation(QuantityImpl.createQuantityImpl(site.getLocation().depth));
    }

    public String getElevation() {
        return FissuresFormatter.formatElevation(QuantityImpl.createQuantityImpl(site.getLocation().elevation));
    }

    public String toString() {
        return SiteIdUtil.toString(get_id());
    }

    private SiteImpl site;

    public void insertIntoContext(VelocityContext ctx) {
        ctx.put("site", this);
        getStation().insertIntoContext(ctx);
    }
}
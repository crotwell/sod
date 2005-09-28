package edu.sc.seis.sod.velocity.network;

import java.text.SimpleDateFormat;
import org.apache.velocity.VelocityContext;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.sc.seis.sod.status.FissuresFormatter;

/**
 * @author groves Created on Jan 7, 2005
 */
public class VelocitySite extends Site {

    public VelocitySite(Site s) {
        this.site = s;
        my_location = s.my_location;
        effective_time = s.effective_time;
        my_station = s.my_station;
        comment = s.comment;
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

    public VelocityStation getStation() {
        return new VelocityStation(site.my_station);
    }

    public VelocityNetwork getNet() {
        return getStation().getNet();
    }

    public MicroSecondDate getStartDate() {
        return new MicroSecondDate(effective_time.start_time);
    }

    public MicroSecondDate getEndDate() {
        return new MicroSecondDate(effective_time.end_time);
    }

    public String getStart() {
        return FissuresFormatter.formatDate(effective_time.start_time);
    }

    public String getStart(String dateFormat) {
        if(dateFormat.equals("longfile")) {
            return FissuresFormatter.formatDateForFile(effective_time.start_time);
        }
        return new SimpleDateFormat(dateFormat).format(new MicroSecondDate(effective_time.start_time));
    }

    public String getEnd() {
        return FissuresFormatter.formatDate(effective_time.end_time);
    }

    public String getComment() {
        return comment;
    }
    
    public String getLatitude() {
        return VelocityStation.df.format(site.my_location.latitude);
    }

    public String getLongitude() {
        return VelocityStation.df.format(site.my_location.longitude);
    }

    public String getOrientedLatitude() {
        if(site.my_location.latitude < 0) {
            return VelocityStation.df.format(-site.my_location.latitude) + " S";
        }
        return VelocityStation.df.format(site.my_location.latitude) + " N";
    }

    public String getOrientedLongitude() {
        if(site.my_location.longitude < 0) {
            return VelocityStation.df.format(-site.my_location.longitude) + " W";
        }
        return VelocityStation.df.format(site.my_location.longitude) + " E";
    }

    public String getDepth() {
        return FissuresFormatter.formatDepth(QuantityImpl.createQuantityImpl(site.my_location.depth));
    }
    
    private Site site;

    public void insertIntoContext(VelocityContext ctx) {
        ctx.put("site", this);
        getStation().insertIntoContext(ctx);
    }
}
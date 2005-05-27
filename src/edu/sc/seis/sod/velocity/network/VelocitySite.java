package edu.sc.seis.sod.velocity.network;

import org.apache.velocity.VelocityContext;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.SiteId;

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

    private Site site;

    public void insertIntoContext(VelocityContext ctx) {
        ctx.put("site", this);
        getStation().insertIntoContext(ctx);
    }
}
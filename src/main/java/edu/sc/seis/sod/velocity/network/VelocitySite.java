package edu.sc.seis.sod.velocity.network;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.model.common.UnknownUnit;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.util.convert.stationxml.StationXMLToFissures;
import org.apache.velocity.VelocityContext;

/**
 * Simple wrapper for old Site, mainly allows getCode to be locCode, and also lat and lon.
 */
public class VelocitySite {

    public VelocitySite(Channel chan) {
        this.chan = chan;
    }

    public String getCode() {
        return chan.getLocCode();
    }

    public String getLatitude() {
        return VelocityStation.df.format(chan.getLatitude().getValue());
    }

    public String getLongitude() {
        return VelocityStation.df.format(chan.getLongitude().getValue());
    }

    public String getOrientedLatitude() {

        String suffix = " N";
        float lat = chan.getLatitude().getValue();
        if(lat < 0) {
            lat = -1 * lat;
            suffix = " S";
        }
        return VelocityStation.df.format(lat) + suffix;
    }


    public String getOrientedLongitude() {
        String suffix = " E";
        float lon = chan.getLongitude().getValue();
        if(lon < 0) {
            lon = -1 * lon;
            suffix = " W";
        }
        return VelocityStation.df.format(lon) + suffix;
    }

    public String getDepth() throws UnknownUnit {
        return FissuresFormatter.formatElevation(StationXMLToFissures.convertFloatType(chan.getDepth()));
    }

    public String getElevation() throws UnknownUnit {
        return FissuresFormatter.formatElevation(StationXMLToFissures.convertFloatType(chan.getElevation()));
    }
    public void insertIntoContext(VelocityContext ctx) {
        ctx.put("site", this);
    }

    public static VelocitySite wrap(Channel chan) {
        return new VelocitySite((Channel)chan);
    }

    Channel chan;
}

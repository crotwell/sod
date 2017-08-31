package edu.sc.seis.sod.velocity.network;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

/**
 * @author groves Created on Jan 7, 2005
 */
public class VelocityChannel extends Channel {

    public VelocityChannel(Channel chan) {
        super(chan.getStation(), chan.getLocCode(), chan.getChannelCode());
        this.chan = chan;
        setSite(chan.getSite());
        setOrientation(chan.getOrientation());
        setSamplingInfo(chan.getSamplingInfo());
        setEffectiveTime(chan.getEffectiveTime());
        setName(chan.getName());
    }

    public ChannelId get_id() {
        return chan.get_id();
    }

    public ChannelId getId() {
        return chan.getId();
    }

    public String get_code() {
        return chan.get_code();
    }

    public float getAzimuth() {
        return getOrientation().azimuth;
    }

    public float getDip() {
        return getOrientation().dip;
    }

    public String getCode() {
        return get_code();
    }

    public String getBandCode() {
        return get_code().substring(0,1);
    }

    public String getGainCode() {
        return get_code().substring(1,2);
    }

    public String getOrientationCode() {
        return get_code().substring(2,3);
    }

    public String getCodes() {
        return getNet().getCode() + "." + getStation().getCode() + "."
                + getSite().getCode() + "." + getCode();
    }

    public VelocityNetwork getNet() {
        return getStation().getNet();
    }

    public VelocityNetwork getNetwork() {
        return getNet();
    }

    public VelocityStation getStation() {
        return getSite().getStation();
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
    
    public Instant getStartDate() {
        return getEffectiveTime().getBeginTime();
    }

    public String getStart() {
        return FissuresFormatter.formatDate(getEffectiveTime().getBeginTime());
    }

    public String getStart(String format) {
        return SimpleVelocitizer.format(getStartDate(), format);
    }

    public Instant getEndDate() {
        return getEffectiveTime().getEndTime();
    }

    public String getEnd() {
        return FissuresFormatter.formatDate(getEffectiveTime().getEndTime());
    }

    public String getEnd(String format) {
        return SimpleVelocitizer.format(getEndDate(), format);
    }

    public VelocitySampling getSampling() {
        return new VelocitySampling(getSamplingInfo());
    }

    public String compactToString() {
        return ChannelIdUtil.toStringNoDates(this);
    }

    public String toString() {
        return ChannelIdUtil.toStringNoDates(chan.get_id());
    }

    public boolean hasDbId() {
        return chan.getDbid() >= 0;
    }

    public int getDbId() {
        if(hasDbId()) {
            return chan.getDbid();
        }
        throw new UnsupportedOperationException("This channel had no dbid");
    }

    public Channel getWrapped() {return chan;}
    
    private Channel chan;

    public static VelocityChannel[] wrap(Channel[] chans) {
        VelocityChannel[] velChans = new VelocityChannel[chans.length];
        for(int i = 0; i < velChans.length; i++) {
            velChans[i] = wrap(chans[i]);
        }
        return velChans;
    }
    
    public static List<VelocityChannel> wrap(List<? extends Channel> chans) {
        List<VelocityChannel> velChans = new ArrayList<VelocityChannel>();
        for(Channel c : chans) {
            velChans.add(wrap(c));
        }
        return velChans;
    }

    public void insertIntoContext(VelocityContext ctx) {
        ctx.put("channel", this);
        ctx.put("chan", this);
        getSite().insertIntoContext(ctx);
    }

    public static VelocityChannel wrap(Channel chan) {
        if(chan instanceof VelocityChannel) {
            return (VelocityChannel)chan;
        }
        return new VelocityChannel((Channel)chan);
    }
}
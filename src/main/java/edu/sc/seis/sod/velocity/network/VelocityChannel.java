package edu.sc.seis.sod.velocity.network;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.util.convert.stationxml.StationXMLToFissures;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

/**
 * @author groves Created on Jan 7, 2005
 */
public class VelocityChannel extends Channel {

    public VelocityChannel(Channel chan) {
        super(chan.getStation(), chan.getLocCode(), chan.getChannelCode());
        this.chan = chan;
    }

    public ChannelId getId() {
        return ChannelId.of(chan);
    }

    public float getAzimuth() {
        return chan.getAzimuth().getValue();
    }

    public float getDip() {
        return chan.getDip().getValue();
    }

    public String getCode() {
        return chan.getCode();
    }

    public String getLocCode() {
        return chan.getLocCode();
    }

    public String getBandCode() {
        return getCode().substring(0,1);
    }

    public String getGainCode() {
        return getCode().substring(1,2);
    }

    public String getOrientationCode() {
        return getCode().substring(2,3);
    }

    public String getCodes() {
        return getNet().getCode() + "." + getStation().getCode() + "."
                + getLocCode() + "." + getCode();
    }

    public VelocityNetwork getNet() {
        return getStation().getNet();
    }

    public VelocityNetwork getNetwork() {
        return getNet();
    }

    public VelocityStation getStation() {
        if (velStation == null) {
            velStation = new VelocityStation(chan.getStation());
        }
        return velStation;
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

    public String getDepth() {
        return FissuresFormatter.formatElevation(StationXMLToFissures.convertFloatType(chan.getDepth()));
    }

    public String getElevation() {
        return FissuresFormatter.formatElevation(StationXMLToFissures.convertFloatType(chan.getElevation()));
    }
    
    public Instant getStartDateTime() {
        return chan.getStartDateTime();
    }

    public String getStart() {
        return FissuresFormatter.formatDate(chan.getStartDateTime());
    }

    public String getStart(String format) {
        return SimpleVelocitizer.format(getStartDateTime(), format);
    }

    public Instant getEndDateTime() {
        return chan.getEndDateTime();
    }

    public String getEnd() {
        return FissuresFormatter.formatDate(chan.getEndDateTime());
    }

    public String getEnd(String format) {
        return SimpleVelocitizer.format(getEndDateTime(), format);
    }

    public VelocitySampling getSampling() {
        return new VelocitySampling(SamplingImpl.of(chan));
    }
    
    public VelocitySensitivity getSensitivity() {
        if (this.chan.getResponse() != null && this.chan.getResponse().getInstrumentSensitivity() != null) {
            return new VelocitySensitivity(this.chan.getResponse().getInstrumentSensitivity());
        } else {
            return null;
        }
    }

    public String compactToString() {
        return ChannelIdUtil.toStringNoDates(this);
    }

    public String toString() {
        return ChannelIdUtil.toStringNoDates(chan);
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
    
    private VelocityStation velStation;

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
        getStation().insertIntoContext(ctx);
    }

    public static VelocityChannel wrap(Channel chan) {
        if(chan instanceof VelocityChannel) {
            return (VelocityChannel)chan;
        }
        return new VelocityChannel((Channel)chan);
    }
}
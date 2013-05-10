package edu.sc.seis.sod.velocity.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.SiteImpl;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

/**
 * @author groves Created on Jan 7, 2005
 */
public class VelocityChannel extends ChannelImpl {

    public VelocityChannel(ChannelImpl chan) {
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

    public VelocitySite getSite() {
        return new VelocitySite((SiteImpl)super.getSite());
    }

    public MicroSecondDate getStartDate() {
        return new MicroSecondDate(getEffectiveTime().start_time);
    }

    public String getStart() {
        return FissuresFormatter.formatDate(getEffectiveTime().start_time);
    }

    public String getStart(String format) {
        return SimpleVelocitizer.format(getStartDate(), format);
    }

    public MicroSecondDate getEndDate() {
        return new MicroSecondDate(getEffectiveTime().end_time);
    }

    public String getEnd() {
        return FissuresFormatter.formatDate(getEffectiveTime().end_time);
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

    public ChannelImpl getWrapped() {return chan;}
    
    private ChannelImpl chan;

    public static VelocityChannel[] wrap(Channel[] chans) {
        VelocityChannel[] velChans = new VelocityChannel[chans.length];
        for(int i = 0; i < velChans.length; i++) {
            velChans[i] = wrap(chans[i]);
        }
        return velChans;
    }
    
    public static List<VelocityChannel> wrap(List<? extends ChannelImpl> chans) {
        List<VelocityChannel> velChans = new ArrayList<VelocityChannel>();
        for(ChannelImpl c : chans) {
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
        return new VelocityChannel((ChannelImpl)chan);
    }
}
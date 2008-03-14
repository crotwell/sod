package edu.sc.seis.sod.velocity.network;

import org.apache.velocity.VelocityContext;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.SiteImpl;
import edu.sc.seis.fissuresUtil.database.network.DBChannel;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

/**
 * @author groves Created on Jan 7, 2005
 */
public class VelocityChannel extends Channel {

    public VelocityChannel(ChannelImpl chan) {
        this.chan = chan;
        my_site = chan.my_site;
        an_orientation = chan.an_orientation;
        sampling_info = chan.sampling_info;
        effective_time = chan.effective_time;
        name = chan.name;
    }

    public ChannelId get_id() {
        return chan.get_id();
    }

    public String get_code() {
        return chan.get_code();
    }

    public float getAzimuth() {
        return an_orientation.azimuth;
    }

    public float getDip() {
        return an_orientation.dip;
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

    public String getName() {
        return name;
    }

    public VelocityNetwork getNet() {
        return getStation().getNet();
    }

    public VelocityStation getStation() {
        return getSite().getStation();
    }

    public VelocitySite getSite() {
        return new VelocitySite((SiteImpl)my_site);
    }

    public MicroSecondDate getStart() {
        return new MicroSecondDate(effective_time.start_time);
    }

    public String getStart(String format) {
        return SimpleVelocitizer.format(getStart(), format);
    }

    public MicroSecondDate getEnd() {
        return new MicroSecondDate(effective_time.end_time);
    }

    public String getEnd(String format) {
        return SimpleVelocitizer.format(getEnd(), format);
    }

    public VelocitySampling getSampling() {
        return new VelocitySampling(sampling_info);
    }

    public String compactToString() {
        return ChannelIdUtil.toStringNoDates(this);
    }

    public String toString() {
        return ChannelIdUtil.toString(chan.get_id());
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
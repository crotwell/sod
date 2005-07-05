package edu.sc.seis.sod.velocity.network;

import org.apache.velocity.VelocityContext;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.network.ChannelIdUtil;

/**
 * @author groves Created on Jan 7, 2005
 */
public class VelocityChannel extends Channel {

    public VelocityChannel(Channel chan) {
        this(chan, -1);
    }

    public VelocityChannel(Channel chan, int dbid) {
        this.chan = chan;
        my_site = chan.my_site;
        an_orientation = chan.an_orientation;
        sampling_info = chan.sampling_info;
        effective_time = chan.effective_time;
        this.dbid = dbid;
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

    public VelocityNetwork getNet() {
        return getStation().getNet();
    }

    public VelocityStation getStation() {
        return getSite().getStation();
    }

    public VelocitySite getSite() {
        return new VelocitySite(my_site);
    }

    public String toString() {
        return ChannelIdUtil.toString(chan.get_id());
    }

    public boolean hasDbId() {
        return dbid >= 0;
    }

    public int getDbId() {
        if(hasDbId()) {
            return dbid;
        }
        throw new UnsupportedOperationException("This channel had no dbid");
    }

    private Channel chan;

    private int dbid;

    public static VelocityChannel[] wrap(Channel[] chans) {
        VelocityChannel[] velChans = new VelocityChannel[chans.length];
        for(int i = 0; i < velChans.length; i++) {
            velChans[i] = new VelocityChannel(chans[i]);
        }
        return velChans;
    }

    public void insertIntoContext(VelocityContext ctx) {
        ctx.put("channel", this);
        getSite().insertIntoContext(ctx);
    }
}
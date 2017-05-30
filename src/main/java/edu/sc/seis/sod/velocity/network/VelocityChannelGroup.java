package edu.sc.seis.sod.velocity.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;

import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.util.display.EventUtil;


public class VelocityChannelGroup {
    
    public VelocityChannelGroup( ChannelGroup wrapped) {
        this.wrapped = wrapped;
    }
    
    public List<VelocityChannel> getChannels() {
        List<VelocityChannel> out = new ArrayList<VelocityChannel>();
        for (int i = 0; i < wrapped.getChannels().length; i++) {
            out.add(new VelocityChannel(wrapped.getChannels()[i]));
        }
        return out;
    }

    public boolean contains(ChannelImpl c) {
        return wrapped.contains(c);
    }

    /**
     * Finds the vertical channel. If no channel has a dip of -90 then null is
     * returned.
     */
    public VelocityChannel getVertical() {
        if (wrapped.getVertical() != null) {
            return new VelocityChannel(wrapped.getVertical());
        }
        return null;
    }

    /**
     * Finds the 2 horizontal channels.
     */
    public List<VelocityChannel> getHorizontal() {
        List<VelocityChannel> out = new ArrayList<VelocityChannel>();
        for (int i = 0; i < wrapped.getHorizontal().length; i++) {
            out.add(new VelocityChannel(wrapped.getHorizontal()[i]));
        }
        return out;
    }

    /**
     * Gets the horizontals as X and Y, so that the first channel's azimuth is
     * the seconds + 90 degrees, ie x == east and y == north. If this is not possible, then a zero length
     * array is returned.
     */
    public List<VelocityChannel> getHorizontalXY() {
        List<VelocityChannel> out = new ArrayList<VelocityChannel>();
        for (int i = 0; i < wrapped.getHorizontalXY().length; i++) {
            out.add(new VelocityChannel(wrapped.getHorizontalXY()[i]));
        }
        return out;
    }

    /**
     * Gets the channel that corresponds to this channelId from the
     * ChannelGroup. The Event is needed in case this channel id comes from a
     * seismogram that has been rotated to GCP, ie it has R or T as its
     * orientation code.
     */
    public VelocityChannel getChannel(ChannelId chanId, CacheEvent event) {
        if (wrapped.getVertical() != null) {
            return new VelocityChannel(wrapped.getChannel(chanId, event));
        }
        return null;
    }

    public VelocityChannel getRadial(CacheEvent event) {
        return getRadial(EventUtil.extractOrigin(event).getLocation());
    }

    public VelocityChannel getRadial(Location eventLoc) {
        return new VelocityChannel(wrapped.getRadial(eventLoc));
    }

    public VelocityChannel getTransverse(CacheEvent event) {
        return getTransverse(EventUtil.extractOrigin(event).getLocation());
    }

    public VelocityChannel getTransverse(Location eventLoc) {
        return new VelocityChannel(wrapped.getTransverse(eventLoc));
    }
    
    public VelocityChannel getChannel1() {
        return new VelocityChannel(wrapped.getChannel1());
    }
    
    public VelocityChannel getChannel2() {
        return new VelocityChannel(wrapped.getChannel2());
    }
    
    public VelocityChannel getChannel3() {
        return new VelocityChannel(wrapped.getChannel3());
    }
    
    public VelocityStation getStation() {
        return new VelocityStation(wrapped.getStation());
    }
    
    public VelocityNetwork getNetworkAttr() {
        return new VelocityNetwork(wrapped.getNetworkAttr());
    }
    
    public void insertIntoContext(VelocityContext ctx) {
        ctx.put("channelGroup", this);
        getChannel1().getSite().insertIntoContext(ctx);
    }
    
    public ChannelGroup getChannelGroup() {
        return wrapped;
    }
    
    ChannelGroup wrapped;
}

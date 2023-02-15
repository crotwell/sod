
package edu.sc.seis.sod.model.station;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.event.CacheEvent;

public class ChannelGroup {

    public ChannelGroup(Channel[] channels) {
        assert channels.length == 3;
        for (int i = 0; i < channels.length; i++) {
            if (channels[i].getStationCode() == null) {
                throw new Error("station null in channel group");
            }
        }
        this.channels = channels;
    }
    
    protected ChannelGroup() {
    }

    protected int dbid;
    
    public void setDbid(int i) {
        dbid = i;
    }
    
    public int getDbid() {
        return dbid;
    }
    
    public Channel[] getChannels() {
        return channels;
    }

    public boolean contains(Channel c) {
        return getIndex(c) != -1;
    }

    /**
     * Finds the vertical channel. If no channel has a dip of -90 then null is
     * returned.
     */
    public Channel getVertical() {
        for(int i = 0; i < channels.length; i++) {
            if(channels[i].getDip().getValue() == -90) {
                return channels[i];
            }
            if(channels[i].getDip().getValue() == 90) {
                // flipped Z, often happens in seed as people think of channel up having positive value
                // even though convention is 90 dip is down
                return channels[i];
            }
        }
        return null;
    }

    /**
     * Finds the 2 horizontal channels.
     */
    public Channel[] getHorizontal() {
        int[] indices = getHorizontalIndices();
        Channel[] out = new Channel[indices.length];
        for(int i = 0; i < indices.length; i++) {
            out[i] = channels[indices[i]];
        }
        return out;
    }

    private int[] getHorizontalIndices() {
        int first = -1;
        for(int i = 0; i < channels.length; i++) {
            if(channels[i].getDip().getValue() == 0) {
                if(first == -1) {
                    first = i;
                } else {
                    return new int[] {first, i};
                }
            }
        }
        if(first == -1) {
            return new int[0];
        } else {
            return new int[] {first};
        }
    }

    /**
     * Gets the horizontals as X and Y, so that the first channel's azimuth is equal to
     * the seconds + 90 degrees, ie x -&gt; east (90) and y -&gt; north (0). If this is not possible, within 2 degrees,
     *  then a zero length array is returned.
     */
    public Channel[] getHorizontalXY() {
        return getHorizontalXY(2);
    }

    /**
     * Gets the horizontals as X and Y, so that the first channel's azimuth is equal to
     * the seconds + 90 degrees, ie x -&gt; east (90) and y -&gt; north (0). 
     * If this is not possible, within tolerance degrees,
     *  then a zero length array is returned.
     */
    public Channel[] getHorizontalXY(float toleranceDegrees) {
        Channel[] out = getHorizontal();
        if(out.length != 2) {
            out = new Channel[0];
        } else if(Math.abs(((360+out[0].getAzimuth().getValue() - out[1].getAzimuth().getValue()) % 360) - 90) < toleranceDegrees ) {
            // in right order
        } else if(Math.abs(((360+out[1].getAzimuth().getValue() - out[0].getAzimuth().getValue()) % 360) - 90) < toleranceDegrees ) {
            Channel tmp = out[0];
            out[0] = out[1];
            out[1] = tmp;
        } else {
            out = new Channel[0];
        }
        return out;
    }

    /**
     * Gets the channel that corresponds to this channelId from the
     * ChannelGroup. The Event is needed in case this channel id comes from a
     * seismogram that has been rotated to GCP, ie it has R or T as its
     * orientation code.
     */
    public Channel getChannel(ChannelId chanId, CacheEvent event) {
        for(int i = 0; i < channels.length; i++) {
            if(ChannelIdUtil.areEqual(chanId, new ChannelId(channels[i]))) {
                return channels[i];
            }
        }
        if (chanId.getNetworkId().equals(channels[0])
                && chanId.getStationCode().equals(channels[0].getStationCode())
                && chanId.getNetworkId().equals(channels[0].getNetworkId())
                && chanId.getChannelCode().substring(0, 2)
                        .equals(channels[0].getCode().substring(0, 2))) {
            if(chanId.getChannelCode().endsWith("R")) {
                return getRadial(event);
            } else if(chanId.getChannelCode().endsWith("T")) {
                return getTransverse(event);
            }
        }
        return null;
    }

    /**
     * replaces the horizontal components with their radial and transverse
     * versions in the ChannelGroup This should only be called if the
     * seismograms that are accompanying this ChannelGroup through the vector
     * process sequence have been rotated.
     */
    public void makeTransverseAndRadial(int transverseIndex,
                                        int radialIndex,
                                        CacheEvent event) {
        channels[radialIndex] = getRadial(event);
        channels[transverseIndex] = getTransverse(event);
    }

    public Channel getRadial(CacheEvent event) {
        return getRadial(event.extractOrigin().getLocation());
    }

    public Channel getRadial(Location eventLoc) {
        DistAz distAz = new DistAz(channels[0], eventLoc);
        return ofAzimuth(channels[0], (float)distAz.getRadialAzimuth(), 'R');
    }

    public Channel getTransverse(CacheEvent event) {
        return getTransverse(event.extractOrigin().getLocation());
    }

    public Channel getTransverse(Location eventLoc) {
        DistAz distAz = new DistAz(channels[0], eventLoc);
        return ofAzimuth(channels[0], (float)distAz.getTransverseAzimuth(), 'T');
    }

    public static Channel ofAzimuth(Channel orig, float azimuth, Character orientationCode) {
        Channel out = new Channel(orig.getStation(),
        		orig.getLocCode(), 
        		replaceChannelOrientationCode(orig.getChannelCode(), orientationCode));
        out.setAzimuth(azimuth);
        out.setDepth(orig.getDepth());
        out.setDescription(orig.getDescription());
        out.setDip(orig.getDip());
        out.setElevation(orig.getElevation());
        out.setEndDateTime(orig.getEndDateTime());
        out.setLatitude(orig.getLatitude());
        out.setLongitude(orig.getLongitude());
        out.setResponse(orig.getResponse());
        out.setSampleRate(orig.getSampleRate());
        out.setStartDateTime(orig.getStartDateTime());
        return out;
    }
    
    private int getIndex(Channel chan) {
        for(int i = 0; i < channels.length; i++) {
            if(channels[i].equals(chan))
                return i;
        }
        // didn't find by object equals, check for ids
        ChannelId chanId = new ChannelId(chan);
        for(int i = 0; i < channels.length; i++) {
            if(ChannelIdUtil.areEqual(new ChannelId(channels[i]), chanId )) {
                return i;
            }
        }
        return -1;
    }
    
    public Channel getChannel1() {
        return getChannels()[0];
    }
    
    public Channel getChannel2() {
        return getChannels()[1];
    }
    
    public Channel getChannel3() {
        return getChannels()[2];
    }
    
    public Station getStation() {
        return getChannel1().getStation();
    }
    
    public Network getNetworkAttr() {
        return getStation().getNetwork();
    }
    
    public boolean areEqual(ChannelGroup other) {
        Channel[] otherChans = other.getChannels();
        for (int i = 0; i < otherChans.length; i++) {
            if (ChannelIdUtil.areEqual(getChannel1(), otherChans[i])) {
                for (int j = 0; j < otherChans.length; j++) {
                    if(j==i) {continue;}
                    if (ChannelIdUtil.areEqual(getChannel2(), otherChans[j])) {
                        for (int k = 0; k < otherChans.length; k++) {
                            if(k==i || k==i) {continue;}
                            if (ChannelIdUtil.areEqual(getChannel3(), otherChans[k])) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private void setChannel(int index, Channel chan) {
        if (channels == null) {
         channels = new Channel[3];   
        }
        channels[index] = chan;
    }
    
    protected void setChannel1(Channel chan) {
        setChannel(0, chan);
    }
    
    protected void setChannel2(Channel chan) {
        setChannel(1, chan);
    }
    
    protected void setChannel3(Channel chan) {
        setChannel(2, chan);
    }

    /** Replace oricatation code (last letter) of channel code.
     * 
     * @param chanCode
     * @param orientation
     * @return
     */
    public static String replaceChannelOrientationCode(String chanCode, Character orientation) {
    	    return chanCode.substring(0, chanCode.length()-1) + orientation;
    }

    public static ChannelId replaceChannelOrientation(ChannelId chanId, String orientation) {
        return new ChannelId(chanId.getNetworkId(),
                             chanId.getStationCode(),
                             chanId.getLocCode(),
                             chanId.getChannelCode().substring(0, 2)
                                     + orientation,
                                     chanId.getStartTime());
    }
    
    private Channel[] channels;

    private static final Logger logger = LoggerFactory.getLogger(ChannelGroup.class);
}

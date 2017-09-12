package edu.sc.seis.sod.process.waveform.vector;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.bag.Rotate;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.Orientation;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.util.display.EventUtil;

/**
 * @author crotwell Created on May 20, 2005
 */
public class RotateGCP implements WaveformVectorProcess, Threadable {

    public RotateGCP(Element el) {
        radialOrientationCode = DOMHelper.extractText(el,
                                                      "radialOrientationCode",
                                                      "R");
        transverseOrientationCode = DOMHelper.extractText(el,
                                                          "transverseOrientationCode",
                                                          "T");
        ninetyDegreeTol = SodUtil.loadFloat(el, "ninetyDegreeTol", ninetyDegreeTol);
    }

    /**
     * 
     */
    public WaveformVectorResult accept(CacheEvent event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
        WaveformVectorResult trimResult = trimmer.accept(event, channelGroup, original, available, seismograms, cookieJar);
        if ( ! trimResult.isSuccess()) {
            return new WaveformVectorResult(false, trimResult.getSeismograms(), new StringTreeBranch(this, false, trimResult.getReason()));
        }
        seismograms = trimResult.getSeismograms();
        // find x & y channel, y should be x+90 degrees and horizontal
        Channel[] horizontal = channelGroup.getHorizontalXY(ninetyDegreeTol);
        if(horizontal.length == 0) {
            Orientation o1 = Orientation.of(channelGroup.getChannel1());
            Orientation o2 = Orientation.of(channelGroup.getChannel2());
            Orientation o3 = Orientation.of(channelGroup.getChannel3());
            return new WaveformVectorResult(seismograms,
                                            new StringTreeLeaf(this,
                                                               false,
                                                               "Channels not rotatable, unable to find horizontals with 90 deg separation: "+o1.azimuth+"/"+o1.dip+" "+o2.azimuth+"/"+o2.dip+" "+o3.azimuth+"/"+o3.dip+" tol="+ninetyDegreeTol));
        }
        if (! Rotate.areRotatable(Orientation.of(horizontal[0]), Orientation.of(horizontal[1]), ninetyDegreeTol)) {
            return new WaveformVectorResult(seismograms,
                                            new StringTreeLeaf(this,
                                                               false,
                                                               "horizontal channels not orthogonal with tol="+ninetyDegreeTol
                                                               +": xAzimuth="
                                                                       + Orientation.of(horizontal[0]).azimuth
                                                                       + " yAzimuth="
                                                                       + Orientation.of(horizontal[1]).azimuth));
        }
        int xIndex = -1, yIndex = -1;
        for(int i = 0; i < seismograms.length; i++) {
            if(seismograms[i].length != 0) {
                if(ChannelIdUtil.areEqual(seismograms[i][0].channel_id,
                                          horizontal[0])) {
                    xIndex = i;
                }
                if(ChannelIdUtil.areEqual(seismograms[i][0].channel_id,
                                          horizontal[1])) {
                    yIndex = i;
                }
            }
        }
        if(xIndex == -1 || yIndex == -1) {
            return new WaveformVectorResult(seismograms,
                                            new StringTreeLeaf(this,
                                                               false,
                                                               "Can't find seismograms to match horizontal channels: xIndex="
                                                                       + xIndex
                                                                       + " yIndex="
                                                                       + yIndex));
        }
        if(seismograms[xIndex].length != seismograms[yIndex].length) {
            return new WaveformVectorResult(seismograms,
                                            new StringTreeLeaf(this,
                                                               false,
                                                               "Seismogram lengths for horizontal channels don't match: "
                                                                       + seismograms[xIndex].length
                                                                       + " != "
                                                                       + seismograms[yIndex].length));
        }
        for(int i = 0; i < seismograms[xIndex].length; i++) {
            if(seismograms[xIndex][i].getNumPoints() != seismograms[yIndex][i].getNumPoints()) {
                return new WaveformVectorResult(seismograms,
                                                new StringTreeLeaf(this,
                                                                   false,
                                                                   i+ " Seismogram num points for horizontal channels don't match: "
                                                                           + seismograms[xIndex][i].getNumPoints()
                                                                           + " != "
                                                                           + seismograms[yIndex][i].getNumPoints()));
            }
        }
        Location staLoc = Location.of(horizontal[0]);
        Location eventLoc = EventUtil.extractOrigin(event).getLocation();
        LocalSeismogramImpl[][] out = new LocalSeismogramImpl[seismograms.length][];
        for(int i = 0; i < out.length; i++) {
            out[i] = new LocalSeismogramImpl[seismograms[i].length];
            System.arraycopy(seismograms[i], 0, out[i], 0, out[i].length);
        }
        for(int i = 0; i < seismograms[xIndex].length; i++) {
            LocalSeismogramImpl[] rot = Rotate.rotateGCP(seismograms[xIndex][i],
                                                         Orientation.of(horizontal[0]),
                                                         seismograms[yIndex][i],
                                                         Orientation.of(horizontal[1]),
                                                         staLoc,
                                                         eventLoc,
                                                         transverseOrientationCode,
                                                         radialOrientationCode,
                                                         ninetyDegreeTol);
            out[xIndex][i] = rot[0];
            out[yIndex][i] = rot[1];
        }
        channelGroup.makeTransverseAndRadial(xIndex, yIndex, event);
        return new WaveformVectorResult(out, new StringTreeLeaf(this, true));
    }

    public boolean isThreadSafe() {
        return true;
    }
    
    private String radialOrientationCode, transverseOrientationCode;
    
    private float ninetyDegreeTol = Rotate.NINTY_DEGREE_TOLERANCE;
    
    private VectorTrim trimmer = new VectorTrim();
}

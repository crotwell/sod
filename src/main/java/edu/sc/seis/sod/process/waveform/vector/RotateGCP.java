package edu.sc.seis.sod.process.waveform.vector;

import org.w3c.dom.Element;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Rotate;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author crotwell Created on May 20, 2005
 */
public class RotateGCP implements WaveformVectorProcess {

    public RotateGCP(Element el) {
        radialOrientationCode = DOMHelper.extractText(el,
                                                      "radialOrientationCode",
                                                      "R");
        transverseOrientationCode = DOMHelper.extractText(el,
                                                          "transverseOrientationCode",
                                                          "T");
    }

    /**
     * 
     */
    public WaveformVectorResult process(CacheEvent event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
        WaveformVectorResult trimResult = trimmer.process(event, channelGroup, original, available, seismograms, cookieJar);
        if ( ! trimResult.isSuccess()) {
            return new WaveformVectorResult(false, trimResult.getSeismograms(), new StringTreeBranch(this, false, trimResult.getReason()));
        }
        seismograms = trimResult.getSeismograms();
        // find x & y channel, y should be x+90 degrees and horizontal
        Channel[] horizontal = channelGroup.getHorizontalXY();
        if(horizontal.length == 0) {
            return new WaveformVectorResult(seismograms,
                                            new StringTreeLeaf(this,
                                                               false,
                                                               "Channels not rotatable."));
        }
        int xIndex = -1, yIndex = -1;
        for(int i = 0; i < seismograms.length; i++) {
            if(seismograms[i].length != 0) {
                if(ChannelIdUtil.areEqual(seismograms[i][0].channel_id,
                                          horizontal[0].get_id())) {
                    xIndex = i;
                }
                if(ChannelIdUtil.areEqual(seismograms[i][0].channel_id,
                                          horizontal[1].get_id())) {
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
        Location staLoc = horizontal[0].getSite().getLocation();
        Location eventLoc = EventUtil.extractOrigin(event).getLocation();
        LocalSeismogramImpl[][] out = new LocalSeismogramImpl[seismograms.length][];
        for(int i = 0; i < out.length; i++) {
            out[i] = new LocalSeismogramImpl[seismograms[i].length];
            System.arraycopy(seismograms[i], 0, out[i], 0, out[i].length);
        }
        for(int i = 0; i < seismograms[xIndex].length; i++) {
            LocalSeismogramImpl[] rot = Rotate.rotateGCP(seismograms[xIndex][i],
                                                         horizontal[0].getOrientation(),
                                                         seismograms[yIndex][i],
                                                         horizontal[1].getOrientation(),
                                                         staLoc,
                                                         eventLoc,
                                                         transverseOrientationCode,
                                                         radialOrientationCode);
            out[xIndex][i] = rot[0];
            out[yIndex][i] = rot[1];
        }
        channelGroup.makeTransverseAndRadial(xIndex, yIndex, event);
        return new WaveformVectorResult(out, new StringTreeLeaf(this, true));
    }

    private String radialOrientationCode, transverseOrientationCode;
    
    private VectorTrim trimmer = new VectorTrim();
}

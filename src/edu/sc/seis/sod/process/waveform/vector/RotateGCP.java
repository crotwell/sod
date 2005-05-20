package edu.sc.seis.sod.process.waveform.vector;

import org.apache.xerces.util.DOMUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Rotate;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;


/**
 * @author crotwell
 * Created on May 20, 2005
 */
public class RotateGCP implements WaveformVectorProcess {

    /**
     *
     */
    public RotateGCP(Element el) {
        NodeList nl = el.getChildNodes();
        for(int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if(n.getNodeName().equals("radialOrientationCode")) {
                radialOrientationCode = DOMUtil.getChildText(n);
            } else if(n.getNodeName().equals("transverseOrientationCode")) {
                transverseOrientationCode = DOMUtil.getChildText(n);
            }
        }
    }

    /**
     *
     */
    public WaveformVectorResult process(EventAccessOperations event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
        // find x & y channel, y should be x+90 degrees and horizontal
        Channel[] horizontal = channelGroup.getHorizontalXY();
        if (horizontal.length == 0) {
            return new WaveformVectorResult(seismograms,
                                            new StringTreeLeaf(this,
                                                               false,
                                                               "Channels not rotatable."));
        }
        int xIndex = -1, yIndex = -1;
        for(int i = 0; i < seismograms.length; i++) {
            if(seismograms[i].length != 0) {
                if (ChannelIdUtil.areEqual(seismograms[i][0].channel_id, horizontal[0].get_id())) {
                    xIndex = i;
                }
                if(ChannelIdUtil.areEqual(seismograms[i][0].channel_id, horizontal[1].get_id())) {
                    yIndex = i;
                }
            }
        }
        if (xIndex == -1 || yIndex == -1) {
            return new WaveformVectorResult(seismograms,
                                            new StringTreeLeaf(this,
                                                               false,
                                                               "Can't find seismograms to match horizontal channels: xIndex="+xIndex+" yIndex="+yIndex));
        }
        if (seismograms[xIndex].length != seismograms[yIndex].length) {
            return new WaveformVectorResult(seismograms,
                                            new StringTreeLeaf(this,
                                                               false,
                                                               "Seismogram lengths for horizontal channels don't match: "+seismograms[xIndex].length+" != "+seismograms[yIndex].length)); 
        }
        Location staLoc = horizontal[0].my_site.my_location;
        Location eventLoc = EventUtil.extractOrigin(event).my_location;
        LocalSeismogramImpl[][] out = new LocalSeismogramImpl[seismograms.length][];
        for(int i = 0; i < out.length; i++) {
            out[i] = new LocalSeismogramImpl[seismograms[i].length];
            System.arraycopy(seismograms[i], 0, out[i], 0, out[i].length);
        }
        for(int i = 0; i < seismograms[xIndex].length; i++) {
            LocalSeismogramImpl[] rot = Rotate.rotateGCP(seismograms[xIndex][i], seismograms[yIndex][i], staLoc, eventLoc, transverseOrientationCode, radialOrientationCode);
            out[xIndex][i] = rot[0];
            out[yIndex][i] = rot[1];
        }
        return new WaveformVectorResult(out,
                                        new StringTreeLeaf(this,
                                                             true));
    }
    
    String radialOrientationCode = "R";
    
    String transverseOrientationCode = "T";
}

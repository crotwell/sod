package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.model.common.Orientation;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class OrientationRange implements ChannelSubsetter {

    public OrientationRange(Element config) {
        azimuth = Float.parseFloat(DOMHelper.extractText(config, "azimuth"));
        dip = Float.parseFloat(DOMHelper.extractText(config, "dip"));
        offset = Float.parseFloat(DOMHelper.extractText(config, "maxOffset"));
    }

    public StringTree accept(Channel c, NetworkSource network) throws Exception {
        Orientation ori = Orientation.of(c);
        double actualDistance = SphericalCoords.distance(ori.dip,
                                                         ori.azimuth,
                                                         dip,
                                                         azimuth);
        return new StringTreeLeaf(this, actualDistance <= offset);
    }

    private float azimuth;

    private float dip;

    private float offset;
}// OrientationRange

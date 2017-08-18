package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AreaSubsetter;

/**
 * SiteArea.java Created: Thu Mar 14 14:02:33 2002
 * 
 * @author Philip Crotwell
 * @version This class is used to represent the subsetter SiteArea. Site Area
 *          implements ChannelSubsetter and can be any one of GlobalArea or BoxArea
 *          or PointDistanceArea or FlinneEngdahlArea.
 */
public class SiteArea extends AreaSubsetter implements ChannelSubsetter, SodElement {

    public SiteArea(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(Channel channel, NetworkSource network) {
    return new StringTreeLeaf(this, super.accept(channel.getSite().getLocation()));
    }
}

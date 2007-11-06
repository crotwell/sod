package edu.sc.seis.sod.subsetter.site;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AreaSubsetter;
import edu.sc.seis.sod.subsetter.channel.ChannelSubsetter;

import org.w3c.dom.Element;

/**
 * SiteArea.java Created: Thu Mar 14 14:02:33 2002
 * 
 * @author <a href="mailto:">Philip Crotwell </a>
 * @version This class is used to represent the subsetter SiteArea. Site Area
 *          implements SiteSubsetter and can be any one of GlobalArea or BoxArea
 *          or PointDistanceArea or FlinneEngdahlArea.
 */
public class SiteArea extends AreaSubsetter implements ChannelSubsetter, SodElement {

    public SiteArea(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(Channel channel, ProxyNetworkAccess network) {
    return new StringTreeLeaf(this, super.accept(channel.my_site.my_location));
    }
}

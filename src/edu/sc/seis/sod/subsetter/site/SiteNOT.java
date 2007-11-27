package edu.sc.seis.sod.subsetter.site;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.channel.ChannelNOT;

/** @Deprecated */
public final class SiteNOT extends ChannelNOT  {

    public SiteNOT(Element config) throws ConfigurationException {
        super(config);
    }
}// SiteNOT

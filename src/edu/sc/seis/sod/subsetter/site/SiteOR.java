package edu.sc.seis.sod.subsetter.site;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.channel.ChannelOR;

/** @Deprecated */
public final class SiteOR extends ChannelOR {

    public SiteOR(Element config) throws ConfigurationException {
        super(config);
    }
}// SiteOR

package edu.sc.seis.sod.subsetter.site;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.channel.ChannelXOR;

public final class SiteXOR extends ChannelXOR {

    public SiteXOR(Element config) throws ConfigurationException {
        super(config);
    }
}// SiteXOR

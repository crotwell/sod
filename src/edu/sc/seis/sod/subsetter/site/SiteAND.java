package edu.sc.seis.sod.subsetter.site;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.channel.ChannelAND;

/** @Deprecated */
public final class SiteAND extends ChannelAND  {

    public SiteAND(Element config) throws ConfigurationException {
        super(config);
    }
    
}// SiteAND

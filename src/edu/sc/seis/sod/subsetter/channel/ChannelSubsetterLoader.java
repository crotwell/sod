package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.SubsetterLoader;
import edu.sc.seis.sod.subsetter.site.SiteSubsetter;
import edu.sc.seis.sod.subsetter.site.SiteSubsetterLoader;


/**
 * @author groves
 * Created on Mar 6, 2005
 */
public class ChannelSubsetterLoader implements SubsetterLoader {


    public Subsetter load(Element el) throws ConfigurationException {
        try {
            Object subsetter = SodUtil.load(el, new String[] {"channel"});
            return (ChannelSubsetter)subsetter;
        } catch(ConfigurationException e) {
            return new SiteSubsetterWrapper((SiteSubsetter)siteLoader.load(el));
        }
    }

    private SiteSubsetterLoader siteLoader = new SiteSubsetterLoader();
}

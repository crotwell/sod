package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.SubsetterLoader;
import edu.sc.seis.sod.subsetter.network.NetworkSubsetter;

/**
 * @author groves Created on Mar 6, 2005
 */
public class StationSubsetterLoader implements SubsetterLoader {

    public Subsetter load(Element el) throws ConfigurationException {
        Object subsetter = SodUtil.load(el, new String[] {"station", "network"});
        if(subsetter instanceof NetworkSubsetter) {
            return new NetworkSubsetterWrapper((NetworkSubsetter)subsetter);
        } else {
            return (StationSubsetter)subsetter;
        }
    }
}
package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import org.apache.log4j.Category;
import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;

public class SomeCoverage implements AvailableDataSubsetter{
    public SomeCoverage (Element config) {}

    public boolean accept(EventAccessOperations event,
                          Channel channel,
                          RequestFilter[] original,
                          RequestFilter[] available, CookieJar cookieJar) {
        // simple impl, probably need more robust
        return available != null && available.length != 0;
    }

    static Category logger =
        Category.getInstance(SomeCoverage.class.getName());

}// SomeCoverage

package edu.sc.seis.sod.subsetter.availableData;

import org.apache.log4j.Category;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.CookieJar;

public class SomeCoverage implements AvailableDataSubsetter {

    public SomeCoverage(Element config) {}

    public boolean accept(EventAccessOperations event,
                          Channel channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          CookieJar cookieJar) {
        // simple impl, probably need more robust
        return available != null && available.length != 0;
    }

    static Category logger = Category.getInstance(SomeCoverage.class.getName());
}// SomeCoverage

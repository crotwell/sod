package edu.sc.seis.sod.subsetter.availableData;

import org.apache.log4j.Category;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;

public class FullCoverage implements AvailableDataSubsetter, SodElement {

    public FullCoverage(Element config) {}

    public boolean accept(EventAccessOperations event,
                          Channel channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          CookieJar cookieJar) {
        // simple impl, probably need more robust
        if(original.length == available.length) { return true; } // end of if
                                                                 // (original.length
                                                                 // ==
                                                                 // available.length)
        return false;
    }

    static Category logger = Category.getInstance(FullCoverage.class.getName());
}// FullCoverage

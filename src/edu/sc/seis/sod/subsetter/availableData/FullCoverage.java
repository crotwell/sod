package edu.sc.seis.sod.subsetter.availableData;

import org.apache.log4j.Category;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.time.CoverageTool;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class FullCoverage implements AvailableDataSubsetter, SodElement {

    public StringTree accept(EventAccessOperations event,
                             Channel channel,
                             RequestFilter[] original,
                             RequestFilter[] available,
                             CookieJar cookieJar) {
        RequestFilter[] uncovered = CoverageTool.notCovered(original, available);
        return new StringTreeLeaf(this, uncovered.length == 0, uncovered.length
                + " uncovered segments");
    }

    static Category logger = Category.getInstance(FullCoverage.class.getName());
}// FullCoverage

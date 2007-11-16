package edu.sc.seis.sod.subsetter.eventStation;

import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import org.w3c.dom.Element;

/**
 * Describe class <code>NullEventStationSubsetter</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 * @version 1.0
 */
public class PassEventStation implements EventStationSubsetter {

    public PassEventStation() {}

    public PassEventStation(Element config) {}

    public StringTree accept(CacheEvent o,
                          Station station,
                          CookieJar cookieJar) {
        return new StringTreeLeaf(this, true);
    }
}// NullEventStationSubsetter

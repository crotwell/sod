package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

/**
 *
 * sample xml file
 * <pre>
 * &lt;stationCode&gt;&lt;value&gt;00&lt;/value&gt;&lt;/stationCode&gt;
 * </pre>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class StationCode implements StationSubsetter {

    public StationCode(Element config) { this.config = config; }

    public boolean accept(Station station) {
        if(station.get_id().station_code.equals(SodUtil.getNestedText(config))) return true;
        else return false;

    }

    private Element config = null;
}

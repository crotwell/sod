package edu.sc.seis.sod.subsetter.station;

import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

/**
 * StationOperator.java
 * sample xml file
 * <pre>
 *   &lt;stationOperator&gt;&lt;value&gt;00&lt;/value&gt;&lt;/stationOperator&gt;
 * </pre>
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class StationOperator implements StationSubsetter {
    public StationOperator (Element config) throws ConfigurationException {
        this.config = config;
    }

    public boolean accept(Station e) {
        if(e.operator.equals(SodUtil.getNestedText(config))) return true;
        else return false;
    }

    Element config;
}// StationOperator


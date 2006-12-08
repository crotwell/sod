package edu.sc.seis.sod.subsetter.station;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import org.w3c.dom.Element;

/**
 * StationName.java
 * sample xml file
 * <pre>
 *   &lt;stationName&gt;&lt;value&gt;00&lt;/value&gt;&lt;/stationName&gt;
 * </pre>
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class StationName implements StationSubsetter {
    public StationName (Element config) throws ConfigurationException {
        this.config = config;
    }

    public StringTree accept(Station e, NetworkAccess network) {
        if(e.name.equals(SodUtil.getNestedText(config))) return new Pass(this);
        else return new Fail(this);
    }

    Element config;
}// StationName

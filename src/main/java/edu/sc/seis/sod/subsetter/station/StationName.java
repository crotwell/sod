package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

/**
 * StationName.java
 * sample xml file
 * <pre>
 *   &lt;stationName&gt;&lt;value&gt;00&lt;/value&gt;&lt;/stationName&gt;
 * </pre>
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author Philip Crotwell
 */
@Deprecated
public class StationName implements StationSubsetter {
    public StationName (Element config) throws ConfigurationException {
        this.config = config;
    }

    public StringTree accept(Station e, NetworkSource network) {
        return new Fail(this);
    }

    Element config;
}// StationName

package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Operator;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

/**
 * StationOperator.java
 * sample xml file
 * <pre>
 *   &lt;stationOperator&gt;&lt;value&gt;00&lt;/value&gt;&lt;/stationOperator&gt;
 * </pre>
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author Philip Crotwell
 */

public class StationOperator implements StationSubsetter {
    public StationOperator (Element config) throws ConfigurationException {
        this.config = config;
    }

    public StringTree accept(Station e, NetworkSource network) {
        for(Operator o : e.getOperatorList()) {
            // ToDo only use first agency as pull request to stationxml will eliminate multiple
            if (o.getAgencyList().get(0).equals(SodUtil.getNestedText(config))) {
                return new Pass(this);
            }
        }
        return new Fail(this);
    }

    Element config;
}// StationOperator


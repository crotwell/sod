package edu.sc.seis.sod.subsetter.station;

import java.util.regex.Pattern;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

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

    public StationCode(Element config) { 
        this.config = config;
        pattern = Pattern.compile(SodUtil.getNestedText(config));
    }

    public StringTree accept(Station station, NetworkAccess network) {
        if(pattern.matcher(station.get_code()).matches()) {
            return new Pass(this);
        } else {
            return new Fail(this);
        }
    }
    
    Pattern pattern;

    private Element config = null;
}

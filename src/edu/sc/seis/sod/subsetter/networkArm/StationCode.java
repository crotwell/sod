package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * 
 * sample xml file
 * <pre>
 * &lt;stationCode&gt;&lt;value&gt;00&lt;/value&gt;&lt;/stationCode&gt;
 * </pre>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class StationCode implements StationIdSubsetter {

    /**
     * Creates a new <code>StationCode</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public StationCode(Element config) {
	    this.config = config;
	
	}

    /**
     * Describe <code>accept</code> method here.
     *
     * @param stationId a <code>StationId</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(StationId stationId, CookieJar cookies) {
	    if(stationId.station_code.equals(SodUtil.getNestedText(config))) return true;
	    else return false;

	}

    private Element config = null;


}

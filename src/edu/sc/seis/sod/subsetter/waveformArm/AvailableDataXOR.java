package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.*;


/**
 * Describe class <code>AvailableDataXOR</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class AvailableDataXOR 
    extends  WaveFormLogicalSubsetter 
    implements AvailableDataSubsetter {
    
    /**
     * Creates a new <code>AvailableDataXOR</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public AvailableDataXOR (Element config) throws ConfigurationException {
	super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param original a <code>RequestFilter[]</code> value
     * @param available a <code>RequestFilter[]</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAccessOperations event, 
			  NetworkAccess network, 
			  Channel channel, 
			  RequestFilter[] original, 
			  RequestFilter[] available, 
			  CookieJar cookies) throws Exception{

	Iterator it = filterList.iterator();
	while (it.hasNext()) {
	    AvailableDataSubsetter filter = (AvailableDataSubsetter)it.next();
	    if (!filter.accept(event, network, channel, original, available, cookies)) {
		return false;
	    }
	}
	return true;
    }

}// AvailableDataXOR

package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


/**
 * networkIDOR contains a sequence of channelSubsetters. The minimum value of the sequence is 0 and
 *the max value of the sequence is unLimited.
 * 
 * sample xml file
 *<body><pre><bold>
 *	&lt;networkIDOR&gt;
 *		&lt;networkCode&gt;&lt;value&gt;II&lt;/value&gt;&lt;/networkCode&gt;
 *		&lt;networkCode&gt;&lt;value&gt;IU&lt;/value&gt;&lt;/networkCode&gt;
 *		&lt;networkCode&gt;&lt;value&gt;SP&lt;/value&gt;&lt;/networkCode&gt;
 *	&lt;/networkIDOR&gt;
 * </bold></pre></body>
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class NetworkIDOR 
    extends  NetworkLogicalSubsetter 
    implements NetworkIdSubsetter {
    
    /**
     * Creates a new <code>NetworkIDOR</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public NetworkIDOR (Element config) throws ConfigurationException {
	super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param e a <code>NetworkId</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(NetworkId e,  CookieJar cookies) throws Exception{
	System.out.println("THe networkID to be checked in NetworkIDOR is "+e.network_code);
	System.out.println("The size of the list is "+ filterList.size());
	Iterator it = filterList.iterator();
	while(it.hasNext()) {
	    System.out.println("In while loop in accept method of NetworkIDOR");
	    NetworkIdSubsetter filter = (NetworkIdSubsetter)it.next();
	    if ( filter.accept(e, cookies)) {
		return true;
	    }
	}
	return false;
    }

}// NetworkIDOR

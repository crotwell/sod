package edu.sc.seis.sod;

import org.w3c.dom.*;

/**
 * NetworkLogicalSubsetter.java
 *
 *
 * Created: Fri Mar 22 13:50:36 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class NetworkLogicalSubsetter extends LogicalSubsetter{
    /**
     * Creates a new <code>NetworkLogicalSubsetter</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public NetworkLogicalSubsetter (Element config) throws ConfigurationException{
	
	super(config);

    }

    /**
     * Describe <code>getPackageName</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getPackageName() {


	return "edu.sc.seis.sod.subsetter.networkArm";

    }
    
}// NetworkLogicalSubsetter

package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;

import org.w3c.dom.*;

/**
 * MagType.java
 *
 *
 * Created: Tue Apr  2 15:22:02 2002
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla</a>
 * @version
 */

public class MagType implements Subsetter{
    /**
     * Creates a new <code>MagType</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public MagType (Element config){
	
	this.config = config;
    }

    /**
     * Describe <code>getType</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getType() {

	return SodUtil.getNestedText(config);

    }

    private Element config;
    
}// MagType

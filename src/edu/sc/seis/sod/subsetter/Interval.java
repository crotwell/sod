package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * Describe class <code>Interval</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class Interval implements Subsetter{

    /**
     * Creates a new <code>Interval</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public Interval(Element config) {
		this.config = config;		
	}

    /**
     * Describe <code>getUnit</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getUnit() {
		return SodUtil.getNestedText(SodUtil.getElement(config,"unit"));
	}

    /**
     * Describe <code>getValue</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getValue() {
		return SodUtil.getNestedText(SodUtil.getElement(config,"value"));
	}

	private Element config;
}//Interval

package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;

import org.w3c.dom.*;

/**
 * Describe class <code>LongitudeRange</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class LongitudeRange extends RangeSubsetter implements SodElement{

    /**
     * Creates a new <code>LongitudeRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public LongitudeRange(Element config)  throws ConfigurationException {

        super(config);
    }
}

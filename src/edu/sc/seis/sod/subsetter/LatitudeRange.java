package edu.sc.seis.sod.subsetter;
import edu.sc.seis.sod.*;

import org.w3c.dom.Element;

/**
 * Describe class <code>LatitudeRange</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class LatitudeRange extends RangeSubsetter implements SodElement{

    /**
     * Creates a new <code>LatitudeRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public LatitudeRange(Element config)  throws ConfigurationException {
        super(config);
    }
}

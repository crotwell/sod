package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * DistanceRange.java
 *
 *
 * Created: Mon Apr  8 16:09:49 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class DistanceRangeSubsetter implements SodElement{

    /**
     * Creates a new <code>DistanceRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public DistanceRangeSubsetter (Element config) throws ConfigurationException{
        processConfig(config);
    }

    /**
     * Describe <code>processConfig</code> method here.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public void processConfig(Element config) throws ConfigurationException{

    NodeList childNodes = config.getChildNodes();
    Element unitRangeElement = null;
    Node node;
    for(int counter = 0; counter < childNodes.getLength(); counter++) {

        node = childNodes.item(counter);
        if(node instanceof Element) {

        String tagName = ((Element)node).getTagName();
        if(tagName.equals("distanceRange")) unitRangeElement = (Element)node;

        }

    }

    unitRange = (edu.iris.Fissures.UnitRange) SodUtil.load(unitRangeElement, "edu.sc.seis.sod.subsetter");

    }

    /**
     * Describe <code>getDistanceRange</code> method here.
     *
     * @return an <code>edu.iris.Fissures.UnitRange</code> value
     */
    public edu.iris.Fissures.UnitRange  getDistanceRange() {

    return unitRange;

    }

    /**
     * Describe <code>getMinDistance</code> method here.
     *
     * @return a <code>Quantity</code> value
     */
    public QuantityImpl getMinDistance() {

    return new QuantityImpl(getDistanceRange().min_value, getDistanceRange().the_units);

    }


    /**
     * Describe <code>getMaxDistance</code> method here.
     *
     * @return a <code>Quantity</code> value
     */
    public QuantityImpl getMaxDistance() {

    return new QuantityImpl(getDistanceRange().max_value, getDistanceRange().the_units);
    }

    private edu.iris.Fissures.UnitRange unitRange = null;

}// DistanceRange

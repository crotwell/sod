package edu.sc.seis.sod.subsetter;

import org.w3c.dom.Element;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;

/**
 * Describe class <code>Interval</code> here.
 * 
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 * @version 1.0
 */
public class Interval implements Subsetter {

    public Interval(Element config) throws ConfigurationException {
        value = Double.parseDouble(SodUtil.getNestedText(SodUtil.getElement(config,
                                                                            "value")));
        Element element = SodUtil.getElement(config, "unit");
        unit = (UnitImpl)SodUtil.load(element, "");//here the second parameter
                                                   // doesnot matter.
    }

    public UnitImpl getUnit() {
        return unit;
    }

    public double getValue() {
        return value;
    }

    public QuantityImpl getQuantity() throws ConfigurationException {
        return new QuantityImpl(getValue(), getUnit());
    }

    public TimeInterval getTimeInterval() throws ConfigurationException {
        return new TimeInterval(getQuantity());
    }

    private UnitImpl unit;

    private double value;
}//Interval

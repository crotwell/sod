package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

/**
 * Unit.java
 *
 *
 * Created: Tue Apr  2 13:54:51 2002
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla</a>
 * @version
 */

public class Unit implements SodElement{
    /**
     * Creates a new <code>Unit</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public Unit (Element config){
        this.config = config;
    }
    
    
    /**
     * Describe <code>getUnit</code> method here.
     *
     * @return an <code>edu.iris.Fissures.Unit</code> value
     */
    public edu.iris.Fissures.Unit getUnit() {
        String unitName = SodUtil.getNestedText(config);
        if(unitName.equals("SECOND")) return UnitImpl.SECOND;
        else if(unitName.equals("MINUTE")) return UnitImpl.MINUTE;
        else if(unitName.equals("HOUR")) return UnitImpl.HOUR;
        else if(unitName.equals("NANOSECOND")) return UnitImpl.NANOSECOND;
        else if(unitName.equals("MILLISECOND")) return UnitImpl.MILLISECOND;
        else if(unitName.equals("MICROSECOND")) return UnitImpl.MICROSECOND;
        else if(unitName.equals("KILOMETER")) return UnitImpl.KILOMETER;
        else if(unitName.equals("METER")) return UnitImpl.METER;
        else if(unitName.equals("AMPERE")) return UnitImpl.AMPERE;
        else if(unitName.equals("DEGREE")) return UnitImpl.DEGREE;
        else if(unitName.equals("NANOMETER")) return UnitImpl.NANOMETER;
        else if(unitName.equals("MICROMETER")) return UnitImpl.MICROMETER;
        else if(unitName.equals("MILLIMETER")) return UnitImpl.MILLIMETER;
        else if(unitName.equals("DAY")) return UnitImpl.DAY;
        else if(unitName.equals("YEAR")) return UnitImpl.DAY;
        return null;
    }
    
    
    private Element config = null;
    
}// Unit

package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * UnitRange.java
 *
 *
 * Created: Tue Apr  2 13:40:14 2002
 *
 * @author <a href="mailto:telukutl@piglet">Srinivasa Telukutla</a>
 * @version
 */

public class UnitRange implements SodElement{
    public UnitRange (Element config) throws Exception{
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
                if(tagName.equals("unitRange")) unitRangeElement = (Element)node;
                if(tagName.equals("unit")) unitRangeElement = config;
            }
        }
        unitRange = (edu.iris.Fissures.UnitRange) SodUtil.load(unitRangeElement, "");
    }
    
    /**
     * Describe <code>getUnitRange</code> method here.
     *
     * @return an <code>edu.iris.Fissures.UnitRange</code> value
     */
    public edu.iris.Fissures.UnitRange  getUnitRange() {
        
        return unitRange;
        
    }
    
    
    private edu.iris.Fissures.UnitRange unitRange = null;
    
}// UnitRange

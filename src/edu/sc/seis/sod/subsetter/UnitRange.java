package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

    public void processConfig(Element config) throws ConfigurationException{
        NodeList childNodes = config.getChildNodes();
        Element unitRangeElement = null;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                if(tagName.equals("unitRange")) unitRangeElement = (Element)node;
                if(tagName.equals("unit")) unitRangeElement = config;
            }
        }
        unitRange = (edu.iris.Fissures.UnitRange) SodUtil.load(unitRangeElement, "");
    }

    public edu.iris.Fissures.UnitRange  getUnitRange() { return unitRange; }

    private edu.iris.Fissures.UnitRange unitRange = null;
}// UnitRange

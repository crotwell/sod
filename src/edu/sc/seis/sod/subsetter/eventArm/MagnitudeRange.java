package edu.sc.seis.sod.subsetter.eventArm;


import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.MagType;
import edu.sc.seis.sod.subsetter.RangeSubsetter;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *<pre>
 * This subsetter specifies the MagnitudeRange
 * &lt;magnitudeRange&gt;
 *   &lt;magType&gt;mb&lt;/magType&gt;
 *   &lt;magType&gt;MS&lt;/magType&gt;
 *   &lt;min&gt;3.5&lt;/min&gt;
 *       &lt;max&gt;8.0&lt;/max&gt;
 *  &lt;/magnitudeRange&gt;
 *
 *         (or)
 *
 * &lt;magnitudeRange&gt;
 *   &lt;magType&gt;mb&lt;/magType&gt;
 *   &lt;magType&gt;MS&lt;/magType&gt;
 *       &lt;max&gt;8.0&lt;/max&gt;
 *  &lt;/magnitudeRange&gt;
 *
 *         (or)
 *
 * &lt;magnitudeRange&gt;
 *   &lt;magType&gt;mb&lt;/magType&gt;
 *   &lt;magType&gt;MS&lt;/magType&gt;
 *   &lt;min&gt;3.5&lt;/min&gt;
 *  &lt;/magnitudeRange&gt;
 *
 *          (or)
 *
 * &lt;magnitudeRange&gt;
 *   &lt;magType&gt;mb&lt;/magType&gt;
 *   &lt;magType&gt;MS&lt;/magType&gt;
 *  &lt;/magnitudeRange&gt;
 *</pre>
 */

public class MagnitudeRange extends RangeSubsetter implements OriginSubsetter{
    /**
     * Creates a new <code>MagnitudeRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public MagnitudeRange (Element config) throws ConfigurationException {
        super(config);
        this.config = config;
    }

    public boolean accept(EventAccessOperations event, EventAttr eventAttr, Origin origin) {
        if(origin.magnitudes[0].value >= getMinValue() &&
           origin.magnitudes[0].value <= getMaxValue()) { return true;
        }else return false;
    }

    public String[] getSearchTypes() throws ConfigurationException{
        ArrayList arrayList = new ArrayList();
        NodeList childNodes = config.getChildNodes();
        for(int counter  = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                if(tagName.equals("magType")){
                    MagType magType = (MagType)SodUtil.load((Element)node, "");
                    arrayList.add(magType.getType());
                }
            }
        }
        String[] searchTypes = new String[arrayList.size()];
        searchTypes = (String[])arrayList.toArray(searchTypes);
        return searchTypes;
    }

    private Element config;
}// MagnitudeRange

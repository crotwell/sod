package edu.sc.seis.sod.subsetter.eventArm;


import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.MagType;
import edu.sc.seis.sod.subsetter.RangeSubsetter;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class MagnitudeRange extends RangeSubsetter implements OriginSubsetter{
    /**
     * Creates a new <code>MagnitudeRange</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public MagnitudeRange (Element config) throws ConfigurationException {
        super(config);
        this.config = config;
        parseSearchTypes();
    }

    public boolean accept(EventAccessOperations event, EventAttr eventAttr, Origin origin) {
        for (int i = 0; i < origin.magnitudes.length; i++) {
            if(origin.magnitudes[i].value >= getMinValue() &&
               origin.magnitudes[i].value <= getMaxValue() ) {
                if (getSearchTypes().length == 0) {
                    // don't care about search types
                    return true;
                }
                for (int j = 0; j < searchTypes.length; j++) {
                    if (origin.magnitudes[i].type.equals(searchTypes[j])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String[] getSearchTypes() {
        return searchTypes;
    }

    protected void parseSearchTypes() throws ConfigurationException{
        List types = new ArrayList();
        NodeList childNodes = config.getChildNodes();
        for(int counter  = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                if(tagName.equals("magType")){
                    MagType magType = (MagType)SodUtil.load((Element)node, "");
                    types.add(magType.getType());
                }
            }
        }
        searchTypes = (String[])types.toArray(new String[types.size()]);
    }

    private String[] searchTypes;

    private Element config;

}// MagnitudeRange

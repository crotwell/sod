package edu.sc.seis.sod.subsetter.origin;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.event.EventDCQuerier;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.MagType;
import edu.sc.seis.sod.subsetter.RangeSubsetter;

public class MagnitudeRange extends RangeSubsetter implements OriginSubsetter {

    public MagnitudeRange() {
        super();
    }

    public MagnitudeRange(Element config) throws ConfigurationException {
        super(config);
        parseSearchTypes(config);
    }

    public StringTree accept(EventAccessOperations event,
                             EventAttr eventAttr,
                             Origin origin) {
        return new StringTreeLeaf(this,
                                  EventDCQuerier.putPassingMagFirst(origin,
                                                                    getMinValue(),
                                                                    getMaxValue(),
                                                                    getSearchTypes()));
    }

    public String[] getSearchTypes() {
        return searchTypes;
    }

    private void parseSearchTypes(Element config) throws ConfigurationException {
        List types = new ArrayList();
        NodeList childNodes = config.getChildNodes();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                if(tagName.equals("magType")) {
                    MagType magType = (MagType)SodUtil.load((Element)node, "");
                    types.add(magType.getType());
                }
            }
        }
        searchTypes = (String[])types.toArray(new String[types.size()]);
    }

    private String[] searchTypes = {};
}// MagnitudeRange

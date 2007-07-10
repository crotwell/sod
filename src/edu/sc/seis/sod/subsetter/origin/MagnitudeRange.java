package edu.sc.seis.sod.subsetter.origin;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
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
        searchTypes = parseSearchElements(config, "magType");
        contributors = parseSearchElements(config, "contributor");
        parseValueTolerance(config);
    }

    public StringTree accept(EventAccessOperations event,
                             EventAttr eventAttr,
                             Origin origin) {
        return new StringTreeLeaf(this, getAcceptable(origin.magnitudes).length > 0);
    }

    public String[] getSearchTypes() {
        return searchTypes;
    }

    public String[] getContributors() {
        return contributors;
    }

    public Magnitude[] getAcceptable(Magnitude[] mags) {
        List matchList = new ArrayList();
        for(int i = 0; i < mags.length; i++) {
            if((contributors.length == 0 || accept(contribAcceptor,
                                                   mags[i],
                                                   contributors))
                    && (searchTypes.length == 0 || accept(typeAcceptor,
                                                          mags[i],
                                                          searchTypes))) {
                matchList.add(mags[i]);
            }
        }
        Magnitude[] matches = (Magnitude[])matchList.toArray(new Magnitude[0]);
        if(matches.length > 0) {
            if(valueTolerance == LARGEST) {
                matches = new Magnitude[] {EventUtil.getLargest(matches)};
            } else if(valueTolerance == SMALLEST) {
                matches = new Magnitude[] {EventUtil.getSmallest(matches)};
            }
        }
        return acceptValues(matches);
    }

    private boolean accept(StringValueAcceptor acceptor,
                           Magnitude mag,
                           String[] values) {
        for(int i = 0; i < values.length; i++) {
            if(acceptor.accept(mag, values[i])) {
                return true;
            }
        }
        return false;
    }

    private Magnitude[] acceptValues(Magnitude[] mags) {
        List accepted = new ArrayList();
        for(int i = 0; i < mags.length; i++) {
            if(accept(mags[i].value)) {
                accepted.add(mags[i]);
            }
        }
        return (Magnitude[])accepted.toArray(new Magnitude[0]);
    }

    private void parseValueTolerance(Element config) {
        if(SodUtil.getElement(config, "largest") != null) {
            valueTolerance = LARGEST;
        } else if(SodUtil.getElement(config, "smallest") != null) {
            valueTolerance = SMALLEST;
        } else {
            valueTolerance = ANY;
        }
    }

    private String[] parseSearchElements(Element config, String elementName)
            throws ConfigurationException {
        List values = new ArrayList();
        NodeList childNodes = config.getChildNodes();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                if(tagName.equals(elementName)) {
                    if(tagName.equals("magType")) {
                        MagType magType = (MagType)SodUtil.load((Element)node,
                                                                "");
                        values.add(magType.getType());
                    } else if(tagName.equals("contributor")) {
                        values.add(SodUtil.getNestedText((Element)node));
                    }
                }
            }
        }
        return (String[])values.toArray(new String[values.size()]);
    }

    private interface StringValueAcceptor {

        boolean accept(Magnitude mag, String fieldValue);
    }

    private StringValueAcceptor typeAcceptor = new StringValueAcceptor() {

        public boolean accept(Magnitude mag, String value) {
            return mag.type != null && mag.type.equals(value);
        }
    };

    private StringValueAcceptor contribAcceptor = new StringValueAcceptor() {

        public boolean accept(Magnitude mag, String value) {
            return mag.contributor != null && mag.contributor.equals(value);
        }
    };
    
    public String toString() {
        String out = super.toString();
        out += " types={";
        for(int i = 0; i < searchTypes.length; i++) {
            out += searchTypes[i];
            if (i != searchTypes.length-1) {
                out += ",";
            }
        }
        out += "} contributors=={";
        for(int i = 0; i < contributors.length; i++) {
            out += contributors[i];
            if (i != contributors.length-1) {
                out += ",";
            }
        }
        out += "}";
        return out;
    }

    private String[] searchTypes = {};

    private String[] contributors = {};

    private int valueTolerance = ANY;

    private static final int ANY = 0;

    private static final int LARGEST = 1;

    private static final int SMALLEST = 2;
}// MagnitudeRange

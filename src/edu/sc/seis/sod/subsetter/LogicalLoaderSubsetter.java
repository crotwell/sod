package edu.sc.seis.sod.subsetter;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import edu.sc.seis.sod.ConfigurationException;

/**
 * @author groves Created on Mar 6, 2005
 */
public abstract class LogicalLoaderSubsetter implements Subsetter {

    public LogicalLoaderSubsetter(Element el) throws ConfigurationException {
        NodeList kids = el.getChildNodes();
        for(int i = 0; i < kids.getLength(); i++) {
            if(kids.item(i) instanceof Element) {
                subsetters.add(getLoader().load((Element)kids.item(i)));
            }
        }
    }

    public abstract SubsetterLoader getLoader();

    protected List subsetters = new ArrayList();
}
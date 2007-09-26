package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.subsetter.network.NetworkSubsetter;


public class EmbeddedNetwork implements StationSubsetter {

    public EmbeddedNetwork(Element config)
            throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                netSubsetter = (NetworkSubsetter)SodUtil.load((Element)node,
                                                                  "network");
                break;
            }
        }
    }

    public StringTree accept(Station station, NetworkAccess network)
            throws Exception {
        StringTree out =  netSubsetter.accept(network.get_attributes());
        return new StringTreeBranch(this, out.isSuccess(), out);
    }
    
    NetworkSubsetter netSubsetter;
    
}

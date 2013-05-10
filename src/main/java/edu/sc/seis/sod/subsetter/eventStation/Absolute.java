package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.Area;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.DepthRange;

public class Absolute extends PhaseInteractionType {

    public Absolute(Element config) {
        super(config);
        this.config = config;
    }

    public void processConfig() throws ConfigurationException {
        NodeList nodeList = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < nodeList.getLength(); counter++) {
            node = nodeList.item(counter);
            if(node instanceof Element) {
                Object obj = SodUtil.load((Element)node, "");
                if(obj instanceof Area) area = (Area)obj;
                else if(obj instanceof DepthRange) depthRange = (DepthRange)obj;
            }
        }
    }

    public edu.iris.Fissures.Area getArea() {
        return this.area;
    }

    public edu.sc.seis.sod.subsetter.DepthRange getDepthRange() {
        return this.depthRange;
    }

    private Area area = null;

    private DepthRange depthRange = null;

    private Element config;
}//Absolute

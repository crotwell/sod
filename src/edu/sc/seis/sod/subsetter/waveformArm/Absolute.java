package edu.sc.seis.sod.subsetter.waveformArm;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.Area;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.DepthRange;

/**
 * sample xml
 *<pre>
 * &lt;absolute&gt;
 *      &lt;boxArea&gt;
 *              &lt;latitudeRange&gt;
 *                      &lt;min&gt;30&lt;/min&gt;
 *                      &lt;max&gt;33&lt;/max&gt;
 *              &lt;/latitudeRange&gt;
 *              &lt;longitudeRange&gt;
 *                      &lt;min&gt;-100&lt;/min&gt;
 *                      &lt;max&gt;100&lt;/max&gt;
 *              &lt;/longitudeRange&gt;
 *      &lt;/boxArea&gt;
 *  &lt;depthRange&gt;
 *          &lt;unitRange&gt;
 *          &lt;unit&gt;KILOMETER&lt;/unit&gt;
 *          &lt;min&gt;-1000&lt;/min&gt;
 *          &lt;max&gt;1000&lt;/max&gt;
 *      &lt;/unitRange&gt;
 *  &lt;/depthRange&gt;
 * &lt;/absolute&gt;
 *
 *                      (or)
 *
 *
  * &lt;absolute&gt;
 *      &lt;boxArea&gt;
 *              &lt;latitudeRange&gt;
 *                      &lt;min&gt;30&lt;/min&gt;
 *                      &lt;max&gt;33&lt;/max&gt;
 *              &lt;/latitudeRange&gt;
 *              &lt;longitudeRange&gt;
 *                      &lt;min&gt;-100&lt;/min&gt;
 *                      &lt;max&gt;100&lt;/max&gt;
 *              &lt;/longitudeRange&gt;
 *      &lt;/boxArea&gt;
 * &lt;/absolute&gt;
 *
 *                      (or)
 *
 * &lt;absolute&gt;
 *  &lt;depthRange&gt;
 *          &lt;unitRange&gt;
 *          &lt;unit&gt;KILOMETER&lt;/unit&gt;
 *          &lt;min&gt;-1000&lt;/min&gt;
 *          &lt;max&gt;1000&lt;/max&gt;
 *      &lt;/unitRange&gt;
 *  &lt;/depthRange&gt;
 * &lt;/absolute&gt;
 *</pre>
 */

public class Absolute extends PhaseInteractionType {

    public Absolute(Element config) throws ConfigurationException{
        super(config);
        this.config = config;
    }

    public void processConfig() throws ConfigurationException{
        NodeList nodeList = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < nodeList.getLength(); counter++) {
            node = nodeList.item(counter);
            if(node instanceof Element) {
                Object obj = SodUtil.load((Element)node, "waveformArm");
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

/**
 * LongShortSignalToNoise.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.waveformArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.LongShortStoN;
import edu.sc.seis.fissuresUtil.bag.LongShortTrigger;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LongShortSignalToNoise implements LocalSeismogramSubsetter {

    public LongShortSignalToNoise(Element config) throws ConfigurationException{
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                Element element = (Element)node;

                if(element.getTagName().equals("longTime")) {
                    longTime = SodUtil.loadTimeInterval(element);
                } else if(element.getTagName().equals("shortTime")) {
                    shortTime = SodUtil.loadTimeInterval(element);
                } else if(element.getTagName().equals("delayTime")) {
                    delayTime = SodUtil.loadTimeInterval(element);
                } else if(element.getTagName().equals("ratio")) {
                    ratio = Float.parseFloat(SodUtil.getNestedText(element));
                }
            }
        }
        sToN = new LongShortStoN(longTime, shortTime, ratio, delayTime);
    }

    public boolean accept(EventAccessOperations event,
                          Channel channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          LocalSeismogramImpl[] seismograms, CookieJar cookieJar) throws Exception {
        for (int i = 0; i < seismograms.length; i++) {
            LongShortTrigger[] triggers = sToN.calcTriggers(seismograms[i]);
            if (triggers.length != 0) {
                return true;
            }
        }
        return false;
    }


    LongShortStoN sToN;

    TimeInterval longTime = new TimeInterval(100, UnitImpl.SECOND);

    TimeInterval shortTime = new TimeInterval(5, UnitImpl.SECOND);

    TimeInterval delayTime = (TimeInterval)shortTime.multiplyBy(2);

    float ratio;
}


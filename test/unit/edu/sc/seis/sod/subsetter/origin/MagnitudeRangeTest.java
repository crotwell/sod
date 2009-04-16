/**
 * MagnitudeRangeTest.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.subsetter.origin;

import junit.framework.TestCase;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockOrigin;
import edu.sc.seis.sod.XMLConfigUtil;

public class MagnitudeRangeTest extends TestCase {

    public void testMag() throws Exception {
        Magnitude mag = new Magnitude();
        mag.type = "mb";
        mag.value = 5.0f;
        Origin origin = MockOrigin.create();
        origin.setMagnitudes(new Magnitude[] {mag});
        EventAttr eventAttr = new EventAttrImpl("test",
                                                ParseRegions.getInstance()
                                                        .getGeographicRegion(7));
        Element element = XMLConfigUtil.parse("<magnitudeRange>"
                + "<magType>mb</magType>" + "<min>5.0</min>" + "<max>6.0</max>"
                + "</magnitudeRange>");
        MagnitudeRange range = new MagnitudeRange(element);
        String searchTypes = " types=";
        for(int i = 0; i < range.getSearchTypes().length; i++) {
            searchTypes += ", " + range.getSearchTypes()[i];
        }
        assertTrue(mag.value + " " + mag.type + "  " + range.getMinValue()
                           + "<" + range.getMaxValue() + " " + searchTypes,
                   range.accept(null, eventAttr, origin).isSuccess());
        mag.type = "Ms";
        assertFalse(mag.value + " " + mag.type + "  " + range.getMinValue()
                            + "<" + range.getMaxValue() + " " + searchTypes,
                    range.accept(null, eventAttr, origin).isSuccess());
        mag.value = 4.9f;
        assertFalse(mag.value + " " + mag.type + "  " + range.getMinValue()
                            + "<" + range.getMaxValue() + " " + searchTypes,
                    range.accept(null, eventAttr, origin).isSuccess());
        mag.value = 6.1f;
        assertFalse(mag.value + " " + mag.type + "  " + range.getMinValue()
                            + "<" + range.getMaxValue() + " " + searchTypes,
                    range.accept(null, eventAttr, origin).isSuccess());
        element = XMLConfigUtil.parse("<magnitudeRange>"
                + "<contributor>FAKE</contributor>" + "<min>5.0</min>"
                + "<max>6.0</max>" + "</magnitudeRange>");
        range = new MagnitudeRange(element);
        String contributors = " contributors=";
        for(int i = 0; i < range.getContributors().length; i++) {
            contributors += ", " + range.getContributors()[i];
        }
        mag.value = 5.0f;
        assertFalse(mag.value + " " + mag.type + "  " + range.getMinValue()
                            + "<" + range.getMaxValue() + " " + contributors,
                    range.accept(null, eventAttr, origin).isSuccess());
        mag.contributor = "FAKE";
        assertTrue(mag.value + " " + mag.type + "  " + range.getMinValue()
                           + "<" + range.getMaxValue() + " " + contributors,
                   range.accept(null, eventAttr, origin).isSuccess());
        element = XMLConfigUtil.parse("<magnitudeRange>" + "<largest/>"
                + "<min>5.0</min>" + "<max>6.0</max>" + "</magnitudeRange>");
        range = new MagnitudeRange(element);
        Magnitude mag2 = new Magnitude("Ms", 6.1f, "FAKE");
        origin.setMagnitudes(new Magnitude[] {mag, mag2});
        assertFalse(mag.value + " " + mag.type + "  " + range.getMinValue()
                            + "<" + range.getMaxValue() + " " + contributors,
                    range.accept(null, eventAttr, origin).isSuccess());
        mag2.value = 6.0f;
        assertTrue(mag.value + " " + mag.type + "  " + range.getMinValue()
                           + "<" + range.getMaxValue() + " " + contributors,
                   range.accept(null, eventAttr, origin).isSuccess());
        element = XMLConfigUtil.parse("<magnitudeRange>" + "<smallest/>"
                + "<min>5.0</min>" + "<max>6.0</max>" + "</magnitudeRange>");
        range = new MagnitudeRange(element);
        mag.value = 4.9f;
        assertFalse(mag.value + " " + mag.type + "  " + range.getMinValue()
                            + "<" + range.getMaxValue() + " " + contributors,
                    range.accept(null, eventAttr, origin).isSuccess());
        mag.value = 5.0f;
        assertTrue(mag.value + " " + mag.type + "  " + range.getMinValue()
                           + "<" + range.getMaxValue() + " " + contributors,
                   range.accept(null, eventAttr, origin).isSuccess());
    }
}
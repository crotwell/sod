/**
 * MagnitudeRangeTest.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.mockFissures.IfEvent.MockOrigin;
import edu.sc.seis.sod.XMLConfigUtil;
import junit.framework.TestCase;
import org.w3c.dom.Element;

public class MagnitudeRangeTest extends TestCase {

    public void testMag() throws Exception {
        Magnitude mag = new Magnitude();
        mag.type = "mb";
        mag.value = 5.0f;
        Origin origin = MockOrigin.create();
        origin.magnitudes = new Magnitude[] { mag };
        EventAttr eventAttr = new EventAttrImpl("test", ParseRegions.getInstance().getGeographicRegion(7));
        Element element = XMLConfigUtil.parse("<magnitudeRange>"+
                                              "<magType>mb</magType>"+
                                              "<min>5.0</min>"+
                                              "<max>6.0</max>"+
                                             "</magnitudeRange>");
        MagnitudeRange range = new MagnitudeRange(element);
        String searchTypes = " types=";
        for (int i = 0; i < range.getSearchTypes().length; i++) {
            searchTypes+= ", "+range.getSearchTypes()[i];
        }
        assertTrue(mag.value+" "+mag.type+"  "+range.getMinValue()+"<"+range.getMaxValue()+" "+searchTypes, range.accept(null, eventAttr, origin));

        mag.type = "Ms";
        assertFalse(mag.value+" "+mag.type+"  "+range.getMinValue()+"<"+range.getMaxValue()+" "+searchTypes, range.accept(null, eventAttr, origin));

        mag.value = 4.9f;
        assertFalse(mag.value+" "+mag.type+"  "+range.getMinValue()+"<"+range.getMaxValue()+" "+searchTypes, range.accept(null, eventAttr, origin));

        mag.value = 6.1f;
        assertFalse(mag.value+" "+mag.type+"  "+range.getMinValue()+"<"+range.getMaxValue()+" "+searchTypes, range.accept(null, eventAttr, origin));
    }
}


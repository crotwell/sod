/**
 * StartTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import java.util.Properties;
import junit.framework.TestCase;

public class StartTest extends TestCase{
    public StartTest(String name){ super(name); }
    
    public void testIntervalPropertyLoad() throws NoSuchFieldException{
        Properties testProps = new Properties();
        String propName = "sod.event.RefreshInterval";
        testProps.setProperty(propName + ".value", "7");
        testProps.setProperty(propName + ".unit", "DAY");
        Start.add(testProps);
        TimeInterval oneWeek = new TimeInterval(7, UnitImpl.DAY);
        assertEquals(oneWeek, Start.getIntervalProp(null, propName));
        assertEquals(oneWeek, Start.getIntervalProp(oneWeek, "Bogus Prop"));
    }
}


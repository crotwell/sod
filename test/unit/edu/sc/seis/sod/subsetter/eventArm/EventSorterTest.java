/**
 * EventSorterTest.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.sod.XMLConfigUtil;
import edu.sc.seis.sod.subsetter.MockFissures;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import org.xml.sax.SAXException;

public class EventSorterTest extends TestCase{
    public EventSorterTest(String name){ super(name); }
    
    public void setUp(){
        epochEvent = MockFissures.createEvent();
        fallEvent = MockFissures.createFallEvent();
        sorter = new EventSorter();
        sorter.add(fallEvent);
        sorter.add(epochEvent);
        incEvents = new EventAccessOperations[3];
        for (int i = 0; i < 3; i++) {
            MicroSecondDate eventTime = new MicroSecondDate((i + 1) * 1000);
            incEvents[i] = MockFissures.createEvent(eventTime,
                                                           i + 1,//magnitude & depth
                                                           incFeRegions[i]);
            sorter.add(incEvents[i]);
        }
    }
    
    public void testAdditionOrdered(){
        EventAccessOperations[] correctOrder = { fallEvent, epochEvent, incEvents[0], incEvents[1], incEvents[2]};
        checkOrdering(correctOrder);
    }
    
    public void testReversed(){
        setSorting("<sorting><addition order=\"reverse\"/></sorting>");
        EventAccessOperations[] correctOrder = { incEvents[2], incEvents[1], incEvents[0], epochEvent, fallEvent};
        checkOrdering(correctOrder);
    }
    
    public void testDateSorted(){
        setSorting("<sorting><time/></sorting>");
        EventAccessOperations[] correctOrder = { epochEvent, incEvents[0], incEvents[1], incEvents[2], fallEvent};
        checkOrdering(correctOrder);
    }
    
    public void testMagnitudeSorted(){
        setSorting("<sorting><magnitude/></sorting>");
        EventAccessOperations[] correctOrder = { incEvents[0], incEvents[1], incEvents[2], fallEvent, epochEvent };
        checkOrdering(correctOrder);
    }
    
    public void testLocationSorted(){
        setSorting("<sorting><location/></sorting>");
        EventAccessOperations[] correctOrder = { incEvents[0], incEvents[1], epochEvent, incEvents[2],  fallEvent  };
        checkOrdering(correctOrder);
    }
    
    private void setSorting(String sorting){
        try {
            sorter.setSorting(XMLConfigUtil.parse(sorting));
        } catch (SAXException e) {} catch (ParserConfigurationException e) {} catch (IOException e) {}
    }
    
    private void checkOrdering(EventAccessOperations[] correctOrder){
        List sorted = sorter.getSortedEvents();
        Iterator it = sorted.iterator();
        for (int i = 0; i < correctOrder.length; i++) {
            assertEquals(correctOrder[i], it.next());
        }
        assertFalse(it.hasNext());
    }
    
    EventSorter sorter;
    
    EventAccessOperations[] incEvents;//3 events that for magnitude, time, and
    //location are increasing in value.  Their locations are Alberta(24) then
    //British Columbia(23), then Central California(39)
    int[] incFeRegions = {24, 23, 39};
    
    EventAccessOperations fallEvent, epochEvent;
}

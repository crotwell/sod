/**
 * EventGroupTemplateTest.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.sod.database.event.EventCondition;
import junit.framework.TestCase;

public class EventGroupTemplateTest extends TestCase{
    public EventGroupTemplateTest(String name){ super(name); }
    
    public void setUp(){
        egt = EventGroupTemplate.createDefaultTemplate();
        epochEvent = MockEventAccessOperations.createEvent();
        berlinEvent = MockEventAccessOperations.createFallEvent();
    }
    
    public void testEmpty(){
        assertEquals("", egt.getResult());
    }
    
    public void testAdd(){
        egt.change(MockEventAccessOperations.createEvent(), EventCondition.NEW);
        assertEquals(epochResult, egt.getResult());
    }
    
    public void testUpdate(){
        egt.change(epochEvent, EventCondition.NEW);
        egt.change(epochEvent, EventCondition.SUBSETTER_PASSED);
        assertEquals(epochResult, egt.getResult());
    }
    
    public void testAddSecondItem(){
        egt.change(epochEvent, EventCondition.NEW);
        egt.change(berlinEvent, EventCondition.NEW);
        assertEquals(epochResult + berlinResult, egt.getResult());
    }
    
    public void testRepeatedAdd(){
        egt.change(epochEvent, EventCondition.NEW);
        egt.change(epochEvent, EventCondition.NEW);
        assertEquals(epochResult, egt.getResult());
    }
    
    private String epochResult = "CENTRAL ALASKA19700101T00:00:00.000";
    private String berlinResult = "GERMANY19900613T12:00:00.000";
    
    EventAccessOperations epochEvent, berlinEvent;
    EventGroupTemplate egt;
}

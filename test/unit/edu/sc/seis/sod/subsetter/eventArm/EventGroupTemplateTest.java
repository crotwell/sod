/**
 * EventGroupTemplateTest.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.subsetter.MockFissures;
import junit.framework.TestCase;

public class EventGroupTemplateTest extends TestCase{
    public EventGroupTemplateTest(String name){ super(name); }
    
    public void setUp(){
        egt = EventGroupTemplate.createDefaultTemplate();
        epochEvent = MockFissures.createEvent();
        berlinEvent = MockFissures.createFallEvent();
    }
    
    public void testEmpty(){
        assertEquals("", egt.getResult());
    }
    
    public void testAdd(){
        egt.change(MockFissures.createEvent(), RunStatus.NEW);
        assertEquals(epochResult, egt.getResult());
    }
    
    public void testUpdate(){
        egt.change(epochEvent, RunStatus.NEW);
        egt.change(epochEvent, RunStatus.PASSED);
        assertEquals(epochResult, egt.getResult());
    }
    
    public void testAddSecondItem(){
        egt.change(epochEvent, RunStatus.NEW);
        egt.change(berlinEvent, RunStatus.NEW);
        assertEquals(epochResult + berlinResult, egt.getResult());
    }
    
    public void testRepeatedAdd(){
        egt.change(epochEvent, RunStatus.NEW);
        egt.change(epochEvent, RunStatus.NEW);
        assertEquals(epochResult, egt.getResult());
    }
    
    private String epochResult = "CENTRAL ALASKA19700101T00:00:00.000";
    private String berlinResult = "GERMANY19900613T12:00:00.000";
    
    EventAccessOperations epochEvent, berlinEvent;
    EventGroupTemplate egt;
}

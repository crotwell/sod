/**
 * EventStatusTemplateTest.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.eventArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.sod.XMLConfigUtil;
import edu.sc.seis.sod.database.event.EventCondition;
import java.io.IOException;
import junit.framework.TestCase;

public class EventStatusTemplateTest extends TestCase{
    public EventStatusTemplateTest(String name){
        super(name);
    }
    
    public void setUp() throws IOException {
        init("<eventStatusTemplate>"
                 + "<fileDir>status</fileDir>"
                 + "<eventConfig xlink:href=\"jar:edu/sc/seis/sod/data/basicEventTemplate.xml\"/>"
                 + "</eventStatusTemplate>");
    }
    
    private void init(String config) throws IOException{
        try {
            temp = new EventStatusTemplate(XMLConfigUtil.parse(config));
        }  catch (Exception e) {
            System.err.println("Unable to open test template file!");
            e.printStackTrace();
        }
        temp.setArmStatus("Setting up");
    }
    
    public void testHTMLTemplate() throws IOException {
        init("<eventStatusTemplate>"
                 + "<fileDir>status</fileDir>"
                 + "<eventConfig xlink:href=\"jar:edu/sc/seis/sod/data/htmlEventTemplate.xml\"/>"
                 + "</eventStatusTemplate>");
        assertEquals(plainHTMLOutput, temp.getResult());
    }
    
    public void testNothingAdded(){
        assertEquals(plainOutput, temp.getResult());
    }
    
    public void testAddEvent() throws Exception {
        temp.change(epochEvent, EventCondition.NEW);
        assertEquals(singleEvent, temp.getResult());
        temp.change(fallEvent, EventCondition.NEW);
        assertEquals(twoEvents, temp.getResult());
    }
    
    public void testUpdate() throws Exception {
        temp.change(epochEvent, EventCondition.NEW);
        temp.change(epochEvent, EventCondition.SUCCESS);
        assertEquals(updatedEvent, temp.getResult());
    }
    
    private EventAccessOperations epochEvent = MockEventAccessOperations.createEvent();
    private EventAccessOperations fallEvent = MockEventAccessOperations.createFallEvent();
    private EventStatusTemplate temp;
    
    private String intro =
        "Event Arm Status:Setting up\n"+
        "These are the events I'm watching:\n";
    
    private String outro = "\naren't they grand?";
    
    private String plainOutput =
        intro +
        outro;
    
    private String plainHTMLOutput = "<html><header><title font=\"testAttr\">Event Arm</title></header><body>Event Arm Status:Setting up\n" +
        "These are the events I'm watching:\n\n" +
        "aren't they grand?<br/>\n"+
        "And here's a link to a map <img src=\"eventMap.png\" alt=\"Setting up\"/>\n"+
        "</body></html>";
    
    private String alaska = "CENTRAL ALASKA19700101T00:00:00.000";
    
    private String singleEvent = intro + alaska + outro;
    
    private String newBerlin = "GERMANY19900613T12:00:00.000";
    
    private String twoEvents = intro + alaska + newBerlin + outro;
    
    private String updatedEvent =  intro + alaska + outro;
}

/**
 * EventStatusTemplateTest.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.eventArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.XMLConfigUtil;
import edu.sc.seis.sod.subsetter.MockFissures;
import junit.framework.TestCase;

public class EventStatusTemplateTest extends TestCase{
    public EventStatusTemplateTest(String name){
        super(name);
    }
    
    public void setUp(){
        init("<eventStatusTemplate xlink:href=\"jar:edu/sc/seis/sod/data/basicEventTemplate.xml\" outputLocation=\"test.txt\"/>");
    }
    
    private void init(String config){
        try {
            temp = new EventStatusTemplate(XMLConfigUtil.parse(config));
        }  catch (Exception e) {
            System.err.println("Unable to open test template file!");
            e.printStackTrace();
        }
        temp.setArmStatus("Setting up");
    }
    
    public void testHTMLTemplate(){
        init("<eventStatusTemplate xlink:href=\"jar:edu/sc/seis/sod/data/htmlEventTemplate.xml\" outputLocation=\"test.txt\"/>");
        assertEquals(plainHTMLOutput, temp.getResult());
    }
    
    public void testNothingAdded(){
        assertEquals(plainOutput, temp.getResult());
    }
    
    public void testAddEvent(){
        temp.change(epochEvent, RunStatus.NEW);
        assertEquals(singleEvent, temp.getResult());
        temp.change(fallEvent, RunStatus.NEW);
        assertEquals(twoEvents, temp.getResult());
    }
    
    public void testUpdate(){
        temp.change(epochEvent, RunStatus.NEW);
        temp.change(epochEvent, RunStatus.PASSED);
        assertEquals(updatedEvent, temp.getResult());
    }
    
    private EventAccessOperations epochEvent = MockFissures.createEvent();
    private EventAccessOperations fallEvent = MockFissures.createFallEvent();
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

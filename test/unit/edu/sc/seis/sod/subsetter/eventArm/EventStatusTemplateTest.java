/**
 * EventStatusTemplateTest.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.eventArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.XMLConfigUtil;
import edu.sc.seis.sod.subsetter.MockFissuresCreator;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class EventStatusTemplateTest extends TestCase{
    public EventStatusTemplateTest(String name){
        super(name);
    }
    
    public void setUp(){
        init("<eventStatusTemplate xlink:link=\"jar:edu/sc/seis/sod/data/basicEventTemplate.xml\" outputLocation=\"test.txt\"/>");
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
        init("<eventStatusTemplate xlink:link=\"jar:edu/sc/seis/sod/data/htmlEventTemplate.xml\" outputLocation=\"test.txt\"/>");
        assertEquals(plainHTMLOutput, temp.toString());
    }
    
    public void testNothingAdded(){
        assertEquals(plainOutput, temp.toString());
    }
    
    public void testAddEvent(){
        temp.change(epochEvent, RunStatus.NEW);
        assertEquals(singleEvent, temp.toString());
        temp.change(fallEvent, RunStatus.NEW);
        assertEquals(twoEvents, temp.toString());
    }
    
    public void testUpdate(){
        temp.change(epochEvent, RunStatus.NEW);
        temp.change(epochEvent, RunStatus.PASSED);
        assertEquals(updatedEvent, temp.toString());
    }
    
    private EventAccessOperations epochEvent = MockFissuresCreator.createEvent();
    private EventAccessOperations fallEvent = MockFissuresCreator.createFallEvent();
    private EventStatusTemplate temp;
    
    private String intro =
        "Event Arm Status:Setting up\n"+
        "These are the events I'm watching:\n";
    
    private String outro = "\naren't they grand?";
    
    private String plainOutput =
        intro +
        "No events"+
        outro;
    
    private String plainHTMLOutput = "<html><header><title font=\"testAttr\">Event Arm</title></header><body>Event Arm Status:Setting up\n" +
        "These are the events I'm watching:\n" +
        "No events\n" +
        "aren't they grand?</body></html>";
    
    private String alaska = "CENTRAL ALASKA19700101T00:00:00.000";
    
    private String singleEvent = intro + alaska + outro;
    
    private String newBerlin = "GERMANY19900613T12:00:00.000";
    
    private String twoEvents = intro + alaska + newBerlin + outro;
    
    private String updatedEvent =  intro + alaska + outro;
}

/**
 * EventPageTest.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.database.Status;
import edu.sc.seis.sod.subsetter.MockFissuresCreator;
import java.io.File;
import junit.framework.TestCase;

public class EventPageTest extends TestCase{
    public EventPageTest(String name){
        super(name);
    }
    
    public void setUp(){
        testPage = new EventChannelPage("Test Title", new File("./test.html"),
                                 MockFissuresCreator.createEvent());
    }
    
    public void testConstruction(){
        assertEquals(emptyPage, testPage.generatePage());
    }
    
    public void testAdd(){
        testPage.add(MockFissuresCreator.createChannel(), Status.COMPLETE_SUCCESS);
        assertEquals(oneChanPage, testPage.generatePage());
    }
    
    private EventChannelPage testPage;
    
    private String EVENT_OUTPUT = "CENTRAL ALASKA 01/01/1970 00:00:00 GMT Mag: 5.0 Depth: 0.00 km";
    
    private String CHANNEL_STRING = "TESTCODE.19700101T00:00:00.000Z.TST.TSTS.BHZ.19700101T00:00:00.000Z";
    
    private String emptyPage =
        "<html>\n" +
        "<header><title>Test Title</title></header>\n" +
        "<body>\n" +
        "<b>Channels for " + EVENT_OUTPUT + ":</b><br>\n" +
        "</body>\n" +
        "</html>";
    
    private String oneChanPage =
        "<html>\n" +
        "<header><title>Test Title</title></header>\n" +
        "<body>\n" +
        "<b>Channels for " + EVENT_OUTPUT + ":</b><br>\n" +
        "COMPLETE_SUCCESS " + CHANNEL_STRING + "<br>\n" +
        "</body>\n" +
        "</html>";
}

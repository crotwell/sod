/**
 * SimpleWaveFormHTMLStatusTest.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.EventChannelPair;
import java.io.IOException;
import junit.framework.TestCase;

public class SimpleWaveFormHTMLStatusTest extends TestCase{
    public SimpleWaveFormHTMLStatusTest(String name){ super(name); }

    public void setUp() throws IOException{
        EventChannelPair ecp = MockECP.getECP();
        try {
            testPage = new SimpleHTMLWaveformStatus(null);
        } catch (ConfigurationException e) {}
        testPage.update(ecp);
    }

    public void testUpdate(){
        assertEquals(oneEventWaveform, testPage.generatePage());
    }

    public void testFormatEvent(){
        assertEquals(EVENT_OUTPUT,
                     testPage.formatEvent(MockEventAccessOperations.createEvent()));
    }

    public void testFilizeEvent(){
        assertEquals(EVENT_FILE_LOC,
                     testPage.fileizeEvent(MockEventAccessOperations.createEvent()));
    }

    private SimpleHTMLWaveformStatus testPage;

    private String EVENT_OUTPUT = "CENTRAL ALASKA | 01/01/1970 00:00:00 GMT | Mag: 5.0 | Depth: 0.00 km";

    private String EVENT_FILE_LOC = "CENTRAL_ALASKA19700101T00_00_00.000/event.html";

    private String oneEventWaveform =
        "<html>\n" +
        "<header><title>Waveform Arm Status</title></header>\n" +
        "<body>\n" +
        "<b>Events in waveform arm:</b><br>\n" +
        "<A HREF=" + EVENT_FILE_LOC + ">" + EVENT_OUTPUT + "</A><br>\n" +
        "<b>Links:</b><br>\n" +
        "</body>\n" +
        "</html>";
}

/**
 * NameGeneratorTest.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.XMLConfigUtil;
import edu.sc.seis.sod.subsetter.MockFissures;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class EventFormatterTest extends TestCase{
    public EventFormatterTest(String name){
        super(name);
    }
    
    public void setUp(){
        epochInAlaska = MockFissures.createEvent();
        fallOfWall = MockFissures.createFallEvent();
    }
    
    public void testRegionName(){
        EventFormatter generator = new EventFormatter(createElement(regionName));
        assertEquals("CENTRAL ALASKA", generator.getResult(epochInAlaska));
        assertEquals("GERMANY", generator.getResult(fallOfWall));
    }
    
    public void testRegionNumber(){
        EventFormatter generator = new EventFormatter(createElement(regionNum));
        assertEquals("1", generator.getResult(epochInAlaska));
        assertEquals("543", generator.getResult(fallOfWall));
    }
    
    public void testDepth(){
        EventFormatter generator = new EventFormatter(createElement(depth));
        assertEquals("0", generator.getResult(epochInAlaska));
        assertEquals("10", generator.getResult(fallOfWall));
    }
    
    public void testLat(){
        EventFormatter generator = new EventFormatter(createElement(latitude));
        assertEquals("0", generator.getResult(epochInAlaska));
        assertEquals("52", generator.getResult(fallOfWall));
    }
    
    public void testLon(){
        EventFormatter generator = new EventFormatter(createElement(longitude));
        assertEquals("0", generator.getResult(epochInAlaska));
        assertEquals("13", generator.getResult(fallOfWall));
    }
    
    public void testMag(){
        EventFormatter generator = new EventFormatter(createElement(mag));
        assertEquals("5", generator.getResult(epochInAlaska));
    }
    
    public void testOriginTime(){
        EventFormatter generator = new EventFormatter(createElement(defaultTime));
        assertEquals("1970_001_00_00_00", generator.getResult(epochInAlaska));
        assertEquals("1990_164_12_00_00", generator.getResult(fallOfWall));
        generator = new EventFormatter(createElement(fancyTime));
        assertEquals("AD19701000000000AM", generator.getResult(epochInAlaska));
        assertEquals("AD1990164120000000PM", generator.getResult(fallOfWall));
    }
    
    public void testGetName(){
        Element complex = createElement("Event" + mag + depth + defaultTime + regionName);
        EventFormatter gen = new EventFormatter(complex);
        assertEquals("Event501970_001_00_00_00CENTRAL ALASKA",
                     gen.getResult(epochInAlaska));
        assertEquals("Event5101990_164_12_00_00GERMANY", gen.getResult(fallOfWall));
        Element reordered = createElement(defaultTime + "Event" + regionName + mag + depth);
        gen = new EventFormatter(reordered);
        assertEquals("1970_001_00_00_00EventCENTRAL ALASKA50",
                     gen.getResult(epochInAlaska));
        assertEquals("1990_164_12_00_00EventGERMANY510", gen.getResult(fallOfWall));
    }
    
    public void testDefaultName(){
        EventFormatter gen = new EventFormatter();
        assertEquals("CENTRAL ALASKA19700101T00:00:00.000",
                     gen.getResult(epochInAlaska));
        assertEquals("GERMANY19900613T12:00:00.000", gen.getResult(fallOfWall));
    }
    
    public void testFilize(){
        assertEquals("_test_____", EventFormatter.filize("\t\n test \t\n:_  "));
    }
    
    public void testComplicatedMarkup(){
        try {
            EventFormatter gen = new EventFormatter(XMLConfigUtil.parse(complicatedMarkup));
            assertEquals(complicatedResults, gen.getResult(fallOfWall));
        } catch (IOException e) {} catch (ParserConfigurationException e) {} catch (SAXException e) {}
    }
    
    private static Element createElement(String innards){
        try {
            return  XMLConfigUtil.parse(encase(innards));
        } catch (IOException e) {} catch (ParserConfigurationException e) {} catch (SAXException e) {}
        return null;
    }
    
    private static String encase(String innards){
        return "<eventDirLabel>"+innards+"</eventDirLabel>";
    }
    
    private String complicatedMarkup =
        "<eventLabel>    <tr>\n" +
        "        <td><feRegionName/></td>\n" +
        "        <td><originTime>yyyyMMdd HH:mm:ss</originTime></td>\n" +
        "        <td><magnitude/></td>\n" +
        "        <td><depth/></td>\n" +
        "    </tr>\n" +
        "</eventLabel>";
    
    private String complicatedResults =
        "    <tr>\n" +
        "        <td>GERMANY</td>\n" +
        "        <td>19900613 12:00:00</td>\n" +
        "        <td>5</td>\n" +
        "        <td>10</td>\n" +
        "    </tr>\n";
    
    private EventAccessOperations epochInAlaska, fallOfWall;
    
    private String mag = "<magnitude/>";
    
    private String depth = "<depth/>";
    
    private String defaultTime = "<originTime>yyyy_DDD_HH_mm_ss</originTime>";
    
    private String fancyTime = "<originTime>GyyyyDHHmmssSSSa</originTime>";
    
    private String regionName = "<feRegionName/>";
    
    private String regionNum = "<feRegionNumber/>";
    
    private String latitude = "<latitude/>";
    
    private String longitude = "<longitude/>";
}

/**
 * NameGeneratorTest.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.XMLConfigUtil;
import edu.sc.seis.sod.subsetter.MockFissuresCreator;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class NameGeneratorTest extends TestCase{
    public NameGeneratorTest(String name){
        super(name);
    }
    
    public void setUp(){
        epochInAlaska = MockFissuresCreator.createEvent();
        fallOfWall = MockFissuresCreator.createFallEvent();
    }
    
    public void testRegionName(){
        NameGenerator generator = new NameGenerator(createElement(regionName));
        assertEquals("CENTRAL ALASKA", generator.getName(epochInAlaska));
        assertEquals("GERMANY", generator.getName(fallOfWall));
    }
    
    public void testRegionNumber(){
        NameGenerator generator = new NameGenerator(createElement(regionNum));
        assertEquals("1", generator.getName(epochInAlaska));
        assertEquals("543", generator.getName(fallOfWall));
    }
    
    public void testDepth(){
        NameGenerator generator = new NameGenerator(createElement(depth));
        assertEquals("0", generator.getName(epochInAlaska));
        assertEquals("10", generator.getName(fallOfWall));
    }
    
    public void testLat(){
        NameGenerator generator = new NameGenerator(createElement(latitude));
        assertEquals("0", generator.getName(epochInAlaska));
        assertEquals("52", generator.getName(fallOfWall));
    }
    
    public void testLon(){
        NameGenerator generator = new NameGenerator(createElement(longitude));
        assertEquals("0", generator.getName(epochInAlaska));
        assertEquals("13", generator.getName(fallOfWall));
    }
    
    public void testMag(){
        NameGenerator generator = new NameGenerator(createElement(mag));
        assertEquals("5", generator.getName(epochInAlaska));
    }
    
    public void testOriginTime(){
        NameGenerator generator = new NameGenerator(createElement(defaultTime));
        assertEquals("1970_001_00_00_00", generator.getName(epochInAlaska));
        assertEquals("1990_164_12_00_00", generator.getName(fallOfWall));
        generator = new NameGenerator(createElement(fancyTime));
        assertEquals("AD19701000000000AM", generator.getName(epochInAlaska));
        assertEquals("AD1990164120000000PM", generator.getName(fallOfWall));
    }
    
    public void testGetName(){
        Element complex = createElement("Event" + mag + depth + defaultTime + regionName);
        NameGenerator gen = new NameGenerator(complex);
        assertEquals("Event501970_001_00_00_00CENTRAL ALASKA",
                     gen.getName(epochInAlaska));
        assertEquals("Event5101990_164_12_00_00GERMANY", gen.getName(fallOfWall));
        Element reordered = createElement(defaultTime + "Event" + regionName + mag + depth);
        gen = new NameGenerator(reordered);
        assertEquals("1970_001_00_00_00EventCENTRAL ALASKA50",
                     gen.getName(epochInAlaska));
        assertEquals("1990_164_12_00_00EventGERMANY510", gen.getName(fallOfWall));
    }
    
    public void testDefaultName(){
        NameGenerator gen = new NameGenerator();
        assertEquals("CENTRAL ALASKA19700101T00:00:00.000",
                     gen.getName(epochInAlaska));
        assertEquals("GERMANY19900613T12:00:00.000", gen.getName(fallOfWall));
    }
    
    public void testFilize(){
        assertEquals("_test_____", NameGenerator.filize("\t\n test \t\n:/  "));
    }
    
    public void testComplicatedMarkup(){
        try {
            NameGenerator gen = new NameGenerator(XMLConfigUtil.parse(complicatedMarkup));
            assertEquals(complicatedResults, gen.getName(fallOfWall));
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

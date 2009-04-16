package edu.sc.seis.sod;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import edu.iris.Fissures.Time;


public class SodUtilTest extends TestCase {

    /*
     * Test method for 'edu.sc.seis.sod.SodUtil.loadTime(Element, boolean)'
     */
    public void testLoadTimeElementBoolean() throws SAXException, IOException, ParserConfigurationException, ConfigurationException {
        //MAY  1 (122), 2004
        Element el = XMLConfigUtil.parse("<startTime><year>2004</year><month>05</month></startTime>"); 
        Time t = SodUtil.loadTime(el, false);
        assertEquals("month without day begin: "+t.date_time, "20040501T"+DAY_START, t.date_time);
        t = SodUtil.loadTime(el, true);
        assertEquals("month without day end: "+t.date_time, "20040531T"+DAY_END, t.date_time);
        // year, month, day 
        el = XMLConfigUtil.parse("<startTime><year>2004</year><month>05</month><day>7</day></startTime>"); 
        t = SodUtil.loadTime(el, false);
        assertEquals("year, month, day begin: "+t.date_time, "20040507T"+DAY_START, t.date_time);
        t = SodUtil.loadTime(el, true);
        assertEquals("year, month, day end: "+t.date_time, "20040507T"+DAY_END, t.date_time);
        // leap year
        el = XMLConfigUtil.parse("<startTime><year>2004</year><month>02</month></startTime>"); 
        t = SodUtil.loadTime(el, false);
        assertEquals("leap year month without day begin: "+t.date_time, "20040201T"+DAY_START, t.date_time);
        t = SodUtil.loadTime(el, true);
        assertEquals("leap year month without day end: "+t.date_time, "20040229T"+DAY_END, t.date_time);
        // only year
        el = XMLConfigUtil.parse("<startTime><year>2004</year></startTime>"); 
        t = SodUtil.loadTime(el, false);
        assertEquals("year without month begin: "+t.date_time, "20040101T"+DAY_START, t.date_time);
        t = SodUtil.loadTime(el, true);
        assertEquals("year without month end: "+t.date_time, "20041231T"+DAY_END, t.date_time);
        
    }
    
    static String DAY_START = "00:00:00.000Z";
    static String DAY_END = "23:59:59.999Z";
}

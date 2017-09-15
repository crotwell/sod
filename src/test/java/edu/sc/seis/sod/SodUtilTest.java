package edu.sc.seis.sod;

import java.io.IOException;
import java.io.StringBufferInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import edu.sc.seis.sod.model.common.UnitImpl;
import junit.framework.TestCase;


public class SodUtilTest extends TestCase {


    public static Element parse(String xmlData) throws SAXException, IOException, ParserConfigurationException{
        Document doc = getDocumentBuilder().parse(new StringBufferInputStream(xmlData));
        return doc.getDocumentElement();
    }
    
    public static DocumentBuilderFactory getDocumentBuilderFactory() {
        if(factory == null) {
            factory = DocumentBuilderFactory.newInstance();
        }
        return factory;
    }

    public static DocumentBuilder getDocumentBuilder()
            throws ParserConfigurationException {
        return getDocumentBuilderFactory().newDocumentBuilder();
    }

    static DocumentBuilderFactory factory = null;

    /*
     * Test method for 'edu.sc.seis.sod.SodUtil.loadTime(Element, boolean)'
     */
    public void testLoadTimeElementBoolean() throws SAXException, IOException, ParserConfigurationException, ConfigurationException {
        //MAY  1 (122), 2004
        Element el = parse("<startTime><year>2004</year><month>05</month></startTime>"); 
        MicroSecondDateSupplier t = SodUtil.loadTime(el, false);
        assertEquals("month without day begin: "+t.load(), "2004-05-01T"+DAY_START, t.load().toString());
        t = SodUtil.loadTime(el, true);
        assertEquals("month without day end: "+t.load(), "2004-05-31T"+DAY_END, t.load().toString());
        // year, month, day 
        el = parse("<startTime><year>2004</year><month>05</month><day>7</day></startTime>"); 
        t = SodUtil.loadTime(el, false);
        assertEquals("year, month, day begin: "+t.load(), "2004-05-07T"+DAY_START, t.load().toString());
        t = SodUtil.loadTime(el, true);
        assertEquals("year, month, day end: "+t.load(), "2004-05-07T"+DAY_END, t.load().toString());
        // leap year
        el = parse("<startTime><year>2004</year><month>02</month></startTime>"); 
        t = SodUtil.loadTime(el, false);
        assertEquals("leap year month without day begin: "+t.load(), "2004-02-01T"+DAY_START, t.load().toString());
        t = SodUtil.loadTime(el, true);
        assertEquals("leap year month without day end: "+t.load(), "2004-02-29T"+DAY_END, t.load().toString());
        // only year
        el = parse("<startTime><year>2004</year></startTime>"); 
        t = SodUtil.loadTime(el, false);
        assertEquals("year without month begin: "+t.load(), "2004-01-01T"+DAY_START, t.load().toString());
        t = SodUtil.loadTime(el, true);
        assertEquals("year without month end: "+t.load(), "2004-12-31T"+DAY_END, t.load().toString());
        
    }
    
    public void testLoadUnit() throws Exception {
        Element el = parse("<unit>HOUR</unit>"); 
        UnitImpl u = SodUtil.loadUnit(el);
        assertTrue(u.isConvertableTo(UnitImpl.SECOND));
    }
    
    static String DAY_START = "00:00:00Z"; // default format trims frac sec if zero
    static String DAY_END = "23:59:59.999Z";
}

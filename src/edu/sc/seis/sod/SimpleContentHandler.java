package edu.sc.seis.sod;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SimpleContentHandler.java
 *
 *
 * Created: Tue Jul  2 09:32:52 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class SimpleContentHandler extends DefaultHandler {
    public SimpleContentHandler (XMLReader parser){
        this.parser = parser;
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts) {
        System.out.println(localName);
    }

    XMLReader parser;

}// SimpleContentHandler

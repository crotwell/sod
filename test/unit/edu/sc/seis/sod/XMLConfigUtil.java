/**
 * XMLConfigUtil.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;
import java.io.IOException;
import java.io.StringBufferInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import edu.sc.seis.fissuresUtil.xml.XMLDataSet;



public class XMLConfigUtil{
    public static Element parse(String xmlData) throws SAXException, IOException, ParserConfigurationException{
        Document doc = XMLDataSet.getDocumentBuilder().parse(new StringBufferInputStream(xmlData));
        return doc.getDocumentElement();
    }
}


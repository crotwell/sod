/**
 * XMLConfigUtil.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import edu.sc.seis.fissuresUtil.xml.XMLDataSet;
import org.w3c.dom.Document;
import java.io.StringBufferInputStream;
import org.xml.sax.SAXException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;



public class XMLConfigUtil
{

    public static Element parse(String xmlData) throws SAXException, IOException, ParserConfigurationException, SAXException, IOException {
        Document doc = XMLDataSet.getDocumentBuilder().parse(new StringBufferInputStream(xmlData));
        return doc.getDocumentElement();
    }

}


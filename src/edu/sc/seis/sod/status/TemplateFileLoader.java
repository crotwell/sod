package edu.sc.seis.sod.status;

import edu.sc.seis.sod.Start;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TemplateFileLoader{
    public static Element getTemplate(Element el) throws MalformedURLException,
        IOException, SAXException, ParserConfigurationException {
        Attr attr =  (Attr)el.getAttributes().getNamedItem("xlink:href");
        if(attr == null || attr.getValue().equals("")) {
            throw new IllegalArgumentException("Expected the passed in element " + el.getNodeName() + " to have a xlink:href attribute, but none found");
        }
        URL loc = null;
        if(attr.getValue().startsWith("jar:")) {
            loc = el.getClass().getClassLoader().getResource(attr.getValue().substring(4));
        } else {
            loc = new URL(attr.getValue());
        }
        Document doc = Start.createDoc(new InputSource(loc.openStream()));
        return (Element)doc.getFirstChild();
    }

    private static String getError(Element el){
        return "Trouble loading template file from element " + el.getNodeName();
    }
}

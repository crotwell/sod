package edu.sc.seis.sod.status;

import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TemplateFileLoader{
    public static Element getTemplate(Element el) throws MalformedURLException,
        IOException, SAXException, ParserConfigurationException {
        Element templateEl = SodUtil.getElement(el, "template");
        if(templateEl == null || SodUtil.getNestedText(templateEl).equals("")) {
            throw new IllegalArgumentException("Expected the passed in element '" + el.getNodeName() + "' to have a template element, but none found");
        }
        return getTemplate(CommonAccess.getLoader(), SodUtil.getNestedText(templateEl));
    }
    
    public static Element getTemplate(ClassLoader cl, String loc) throws MalformedURLException, SAXException, ParserConfigurationException, IOException{
        URL url = getUrl(cl, loc);
        InputStream in = url.openStream();
        Document doc = Start.createDoc(new InputSource(in), loc);
        return (Element)doc.getFirstChild();

    }

    public static URL getUrl(ClassLoader cl, String loc) throws MalformedURLException{
        URL url = null;
        if(loc.startsWith("jar:")) {
            url =cl.getResource(loc.substring(4));
        } else {
            url = new URL(loc);
        }
        return url;
    }
}

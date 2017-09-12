package edu.sc.seis.sod;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author groves Created on Feb 17, 2005
 */
public class DOMHelper {

    public static Element getElement(Element el, String name) {
        return (Element)getElements(el, name).item(0);
    }

    public static NodeList getElements(Element el, String name) {
        return extractNodes(el, name);
    }

    public static boolean hasElement(Element el, String name) {
        try {
            return XPathAPI.selectNodeList(el, name).getLength() > 0;
        } catch(TransformerException e) {
            handle(e, name);
        }
        throw new RuntimeException("Should be unreachable");
    }

    public static String extractText(Element el, String xpath) {
        return extractText(el, xpath, null);
    }

    public static String extractText(Element el,
                                     String xpath,
                                     String defaultValue) {
        return extractText(el, xpath, defaultValue, false);
    }

    public static String extractText(Element el,
                                     String xpath,
                                     String defaultValue,
                                     boolean emptyElementMeansEmptyString) {
        try {
            Node n = XPathAPI.selectSingleNode(el, xpath + "/text()");
            if(n == null) {
                // See if the element is there with no text content
                if(emptyElementMeansEmptyString
                        && XPathAPI.selectSingleNode(el, xpath) != null) {
                    return "";
                } else if(defaultValue == null) {
                    throw new RuntimeException("No nodes found matching XPath "
                            + xpath);
                }
                return defaultValue;
            }
            return n.getNodeValue();
        } catch(DOMException e) {
            handle(e);
        } catch(TransformerException e) {
            handle(e, xpath + "/text()");
        }
        throw new RuntimeException("Should be unreachable");
    }

    public static NodeList extractNodes(Element el, String xpath) {
        try {
            return XPathAPI.selectNodeList(el, xpath);
        } catch(DOMException e) {
            handle(e);
        } catch(TransformerException e) {
            handle(e, xpath);
        }
        throw new RuntimeException("Should be unreachable");
    }

    public static Element extractElement(Element el, String xpath) {
        try {
            return (Element)XPathAPI.selectSingleNode(el, xpath);
        } catch(DOMException e) {
            handle(e);
        } catch(TransformerException e) {
            handle(e, xpath);
        }
        throw new RuntimeException("Should be unreachable");
    }

    public static void handle(DOMException e) {
        throw new RuntimeException("This DOMException seems like some sort of library error.  Don't know what I could do further up the stack, so I just wrapped it in this runtime exception.",
                                   e);
    }

    public static void handle(TransformerException e, String xpath) {
        throw new RuntimeException("Caught a transformation exception!  This probably means the XPath "
                                           + xpath + " is screwed up.",
                                   e);
    }

    public static Element createElement(String loc) throws Exception {
        ClassLoader cl = DOMHelper.class.getClassLoader();
        InputStream source = cl.getResourceAsStream(loc);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(source);
        return doc.getDocumentElement();
    }

    public static Element extractOrCreateElement(Element parent, String name) {
        Document rootDoc = parent.getOwnerDocument();
        if(hasElement(parent, name)) {
            return extractElement(parent, name);
        }
        Element newElement = rootDoc.createElement(name);
        parent.appendChild(newElement);
        return newElement;
    }

    public static Text extractOrCreateTextNode(Element filenameElement,
                                               String defaultText) {
        Document rootDoc = filenameElement.getOwnerDocument();
        if(filenameElement.getChildNodes().item(0) instanceof Text) {
            return (Text)filenameElement.getChildNodes().item(0);
        }
        Text textNode = rootDoc.createTextNode(defaultText);
        filenameElement.appendChild(textNode);
        return textNode;
    }

    public static float extractFloat(Element config,
                                     String xpath,
                                     float defaultValue) {
        String text = extractText(config, xpath, DEFAULT);
        if(text.equals(DEFAULT)) {
            // didn't find it, so use default value
            return defaultValue;
        }
        return Float.parseFloat(text);
    }

    public static double extractDouble(Element config,
                                       String xpath,
                                       double defaultValue) {
        String text = extractText(config, xpath, DEFAULT);
        if(text.equals(DEFAULT)) {
            // didn't find it, so use default value
            return defaultValue;
        }
        return Double.parseDouble(text);
    }

    public static int extractInt(Element config, String xpath, int defaultValue) {
        String text = extractText(config, xpath, DEFAULT);
        if(text.equals(DEFAULT)) {
            // didn't find it, so use default value
            return defaultValue;
        }
        return Integer.parseInt(text);
    }

    public static Text getTextChildFromPossiblyNonexistentElement(Element parentOfElement,
                                                                  String elementName,
                                                                  String defaultText) {
        Element filenameElement = extractOrCreateElement(parentOfElement,
                                                         elementName);
        return extractOrCreateTextNode(filenameElement, defaultText);
    }

    private static final String DEFAULT = "DEFAULT";

    public static boolean extractBoolean(Element el, String elementName) {
        return Boolean.parseBoolean(extractText(el, elementName));
    }
}
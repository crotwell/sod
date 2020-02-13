package edu.sc.seis.sod;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import edu.sc.seis.sod.util.exceptionHandler.Extractor;
import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;


/**
 * XMLUtil.java Created: Wed Jun 12 10:03:01 2002
 * 
 * @author <a href="mailto:">Philip Crotwell </a>
 * @version
 */
@Deprecated
public class XMLUtil {
    
    private static boolean needToRegisteredWithGEH = true;
    
    public static void registerExtractorWithExceptionHandler() {
        // don't register twice
        if (needToRegisteredWithGEH) {
            needToRegisteredWithGEH = false;
            GlobalExceptionHandler.add(new Extractor() {
                
                public boolean canExtract(Throwable throwable) {
                    return (throwable instanceof XMLStreamException);
                }

                public String extract(Throwable throwable) {
                    String out = "";
                    if(throwable instanceof XMLStreamException) {
                        XMLStreamException mie = (XMLStreamException)throwable;
                        out += "XML Location: " + mie.getLocation() + "\n";
                    }
                    return out;
                }

                public Throwable getSubThrowable(Throwable throwable) {
                    if(throwable instanceof XMLStreamException) {
                        return ((XMLStreamException)throwable).getNestedException();
                    }
                    return null;
                }
            });
        }
    }
    
    static {
        registerExtractorWithExceptionHandler();
    }

    // ---------------------------------------------------------
    // Begin StAX stuff. DOM stuff is at the bottom.
    // ---------------------------------------------------------
    /**
     * outputs a text element to a StAX writer
     */
    public static void writeTextElement(XMLStreamWriter writer,
                                        String elementName,
                                        String value) throws XMLStreamException {
        writer.writeStartElement(elementName);
        writer.writeCharacters(value);
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    public static void writeEndElementWithNewLine(XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }

    public static XMLStreamReader getXMLStreamReader(File file)
            throws XMLStreamException, FileNotFoundException {
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        return getStaxInputFactory().createXMLStreamReader(inputStream);
    }

    /**
     * Returns a StAXFileWriter without the root element being closed so that it
     * can be appended to. You are responsible for calling the close() method
     * when you are done appending so that the file is written and any open
     * start elements are closed.
     */
    public static StAXFileWriter openXMLFileForAppending(File file)
            throws IOException, XMLStreamException {
        FileReader fileReader = new FileReader(file);
        XMLStreamReader xmlReader = getXMLStreamReader(file);
        StAXFileWriter staxWriter = new StAXFileWriter(file);
        QName rootTag = emptyName;
        int i = xmlReader.next();
        while(xmlReader.hasNext()) {
            if(i != XMLStreamConstants.END_ELEMENT
                    || !xmlReader.getName().equals(rootTag)) {
                // grab the real root element QName
                if(i == XMLStreamConstants.START_ELEMENT
                        && rootTag.equals(emptyName)) {
                    rootTag = xmlReader.getName();
                }
                translateAndWrite(xmlReader, staxWriter.getStreamWriter());
            }
            i = xmlReader.next();
        }
        xmlReader.close();
        fileReader.close();
        return staxWriter;
    }

    public static void translateAndWrite(XMLStreamReader reader,
                                         XMLStreamWriter writer)
            throws XMLStreamException {
        translateAndWrite(reader, writer, true);
    }

    public static void translateAndWrite(XMLStreamReader reader,
                                         XMLStreamWriter writer,
                                         boolean newlinesAfterEndElements)
            throws XMLStreamException {
        int type = reader.getEventType();
        switch(type){
            case XMLStreamConstants.START_DOCUMENT:
                writer.writeStartDocument(reader.getEncoding(),
                                          reader.getVersion());
                break;
            case XMLStreamConstants.END_DOCUMENT:
                writer.writeEndDocument();
                break;
            case XMLStreamConstants.START_ELEMENT:
                String prefix = reader.getPrefix() == null ? ""
                        : reader.getPrefix();
                writer.writeStartElement(prefix,
                                         reader.getLocalName(),
                                         reader.getNamespaceURI());
                for(int i = 0; i < reader.getNamespaceCount(); i++) {
                    writer.writeNamespace(reader.getNamespacePrefix(i),
                                          reader.getNamespaceURI(i));
                }
                for(int i = 0; i < reader.getAttributeCount(); i++) {
                    if(reader.getAttributePrefix(i) != null) {
                        writer.writeAttribute(reader.getAttributePrefix(i),
                                              reader.getAttributeNamespace(i),
                                              reader.getAttributeLocalName(i),
                                              reader.getAttributeValue(i));
                    } else if(reader.getAttributeNamespace(i) != null) {
                        writer.writeAttribute(reader.getAttributeNamespace(i),
                                              reader.getAttributeLocalName(i),
                                              reader.getAttributeValue(i));
                    } else {
                        writer.writeAttribute(reader.getAttributeLocalName(i),
                                              reader.getAttributeValue(i));
                    }
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if(newlinesAfterEndElements) {
                    XMLUtil.writeEndElementWithNewLine(writer);
                } else {
                    writer.writeEndElement();
                }
                break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                writer.writeProcessingInstruction(reader.getPITarget(),
                                                  reader.getPIData());
                break;
            case XMLStreamConstants.CHARACTERS:
                if(!reader.getText().equals("\n")) {
                    writer.writeCharacters(reader.getText());
                }
                break;
            case XMLStreamConstants.COMMENT:
                writer.writeComment(reader.getText());
                break;
        }
    }

    public static String readEvent(XMLStreamReader reader) {
        StringBuffer buf = new StringBuffer();
        int type = reader.getEventType();
        switch(type){
            case XMLStreamConstants.START_DOCUMENT:
                break;
            case XMLStreamConstants.END_DOCUMENT:
                break;
            case XMLStreamConstants.START_ELEMENT:
                buf.append('<');
                if(reader.getPrefix() != null && reader.getPrefix() != "") {
                    buf.append(reader.getPrefix() + ":");
                }
                buf.append(reader.getLocalName());
                for(int i = 0; i < reader.getNamespaceCount(); i++) {
                    buf.append(" xmlns:" + reader.getNamespacePrefix(i) + "=\""
                            + reader.getNamespaceURI(i) + '\"');
                }
                for(int i = 0; i < reader.getAttributeCount(); i++) {
                    buf.append(' ');
                    if(reader.getAttributePrefix(i) != null && reader.getAttributePrefix(i) != "") {
                        buf.append(reader.getAttributePrefix(i) + ":");
                    }
                    buf.append(reader.getAttributeLocalName(i) + "=\""
                            + reader.getAttributeValue(i) + '\"');
                }
                buf.append('>');
                break;
            case XMLStreamConstants.END_ELEMENT:
                buf.append("</");
                if(reader.getPrefix() != null && reader.getPrefix() != "") {
                    buf.append(reader.getPrefix() + ":");
                }
                buf.append(reader.getLocalName() + '>');
                break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                break;
            case XMLStreamConstants.CHARACTERS:
                buf.append(reader.getText());
                break;
            case XMLStreamConstants.COMMENT:
                break;
        }
        return buf.toString();
    }

    public static void gotoNextStartElement(XMLStreamReader parser, String name)
            throws XMLStreamException {
        while(parser.hasNext()) {
            int event = parser.next();
            if(event == XMLStreamConstants.START_ELEMENT) {
                if(parser.getLocalName().equals(name)) {
                    return;
                }
            }
        }
    }

    public static void getNextStartElement(XMLStreamReader parser)
            throws XMLStreamException {
        while(parser.hasNext()) {
            int event = parser.next();
            if(event == XMLStreamConstants.START_ELEMENT) {
                return;
            }
        }
    }

    public static XMLOutputFactory getStaxOutputFactory() {
        if (staxOutputFactory == null) {
            staxOutputFactory = XMLOutputFactory.newInstance();
        }
        return staxOutputFactory;
    }

    public static XMLInputFactory getStaxInputFactory() {
        if (staxInputFactory == null) {
            staxInputFactory = XMLInputFactory.newInstance();
        }
        return staxInputFactory;
    }

    public static XMLEventFactory getStaxEventFactory() {
        if (staxEventFactory == null) {
            staxEventFactory = XMLEventFactory.newInstance();
        }
        return staxEventFactory;
    }
    
    private static XMLOutputFactory staxOutputFactory;

    private static XMLInputFactory staxInputFactory;

    private static XMLEventFactory staxEventFactory;

    public static QName emptyName = new QName("",
                                              "thisisareallyuglynameforatagthathopefullynoonewilleveruse");

    // ---------------------------------------------------------
    // End of StAX stuff. Everything else is DOM.
    // ---------------------------------------------------------
    public static Element createTextElement(Document doc,
                                            String elementName,
                                            String value) {
        Element element = doc.createElement(elementName);
        Text text = doc.createTextNode(value);
        element.appendChild(text);
        return element;
    }

    /**
     * Describe <code>evalNodeList</code> method here.
     * 
     * @param context
     *            a <code>Node</code> value
     * @param path
     *            a <code>String</code> value
     * @return a <code>NodeList</code> value
     * @throws XPathExpressionException 
     */
    public static NodeList evalNodeList(Node context, String path) throws XPathExpressionException {
            // xpath = new CachedXPathAPI();
        	// 1. Instantiate an XPathFactory.
        	  javax.xml.xpath.XPathFactory factory = 
        	                    javax.xml.xpath.XPathFactory.newInstance();
        	  
        	  // 2. Use the XPathFactory to create a new XPath object
        	  javax.xml.xpath.XPath xpath = factory.newXPath();
        	  
        	  // 3. Compile an XPath string into an XPathExpression
        	  javax.xml.xpath.XPathExpression expression = xpath.compile(path);
        	  
        	  // 4. Evaluate the XPath expression on an input document
        	 Object result = expression.evaluate(context, XPathConstants.NODESET);
        	  
            if(result != null && result instanceof NodeList) {
                return (NodeList)result;
            }
        return null;
    }

    /**
     * Describe <code>evalString</code> method here.
     * 
     * @param context
     *            a <code>Node</code> value
     * @param path
     *            a <code>String</code> value
     * @return a <code>String</code> value
     */
    public static String evalString(Node context, String path) {
        try {
			return evalNodeList(context, path).item(0).getNodeValue();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }

    /**
     * returns the concatenation of all text children within the node. Does not
     * recurse into subelements.
     */
    public static String getText(Element config) {
        if(config == null)
            return new String("");
        NodeList children = config.getChildNodes();
        Node node;
        String out = "";
        for(int i = 0; i < children.getLength(); i++) {
            node = children.item(i);
            if(node instanceof Text) {
                out += node.getNodeValue();
            }
        }
        return out;
    }

    /**
     * returns the element with the given name
     */
    public static Element getElement(Element config, String elementName) {
        NodeList children = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < children.getLength(); counter++) {
            node = children.item(counter);
            if(node instanceof Element) {
                if(((Element)node).getTagName().equals(elementName)) {
                    return ((Element)node);
                }
            }
        }
        return null;
    }

    public static Element[] getElementArray(Element config, String elementName) {
        NodeList children = config.getChildNodes();
        Node node;
        ArrayList arrayList = new ArrayList();
        for(int counter = 0; counter < children.getLength(); counter++) {
            node = children.item(counter);
            if(node instanceof Element) {
                if(((Element)node).getTagName().equals(elementName)) {
                    arrayList.add(node);
                }
            }
        }
        Element[] elementArray = new Element[arrayList.size()];
        elementArray = (Element[])arrayList.toArray(elementArray);
        return elementArray;
    }

    /**
     * Describe <code>getAllAsStrings</code> method here.
     * 
     * @param path
     *            a <code>String</code> value
     * @return a <code>String[]</code> value
     * @throws XPathException 
     */
    public static String[] getAllAsStrings(Element config, String path) throws XPathException {
        // logger.debug("The path that is passed to GetALLASStrings is "+path);
        NodeList nodes = evalNodeList(config, path);
        if(nodes == null) {
            return new String[0];
        } // end of if (nodes == null)
        String[] out = new String[nodes.getLength()];
        // logger.debug("the length of the nodes is "+nodes.getLength());
        for(int i = 0; i < out.length; i++) {
            out[i] = nodes.item(i).getNodeValue();
        } // end of for (int i=0; i++; i<out.length)
        return out;
    }

    /**
     * Describe <code>getUniqueName</code> method here.
     * 
     * @param nameList
     *            a <code>String[]</code> value
     * @param name
     *            a <code>String</code> value
     * @return a <code>String</code> value
     */
    public static String getUniqueName(String[] nameList, String name) {
        int counter = 0;
        for(int i = 0; i < nameList.length; i++) {
            if(nameList[i].indexOf(name) != -1)
                counter++;
        }
        if(counter == 0)
            return name;
        return name + "_" + (counter + 1);
    }

    /**
     * Describe <code>evalElement</code> method here.
     * 
     * @param context
     *            a <code>Node</code> value
     * @param path
     *            a <code>String</code> value
     * @return an <code>Element</code> value
     * @throws XPathException 
     */
    public static Element evalElement(Node context, String path) throws XPathException {
        NodeList nList = evalNodeList(context, path);
        if(nList != null && nList.getLength() != 0) {
            return (Element)nList.item(0);
        }
        return null;
    }

}// XMLUtil

package edu.sc.seis.sod;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.log4j.Category;

public class SimpleErrorHandler implements ErrorHandler {

    //not sure why this is needed???
    boolean warmup=false;
    
    boolean foundError = false;

            /** Warning. */
    public void warning(SAXParseException ex) throws SAXException {
    logger.warn(getLocationString(ex), ex);
        foundError = true;
    }

    /** Error. */
    public void error(SAXParseException ex)  throws SAXException {
        if (warmup)
            return;
        foundError = true;
    logger.error(getLocationString(ex), ex);
    }

    /** Fatal error. */
    public void fatalError(SAXParseException ex) throws SAXException {
        if (warmup)
            return;

        logger.fatal(getLocationString(ex), ex);
                foundError = true;
        throw ex;
    }

    public boolean isfoundError() {
        return foundError;
    }
    
    /** Returns a string of the location. */
    private String getLocationString(SAXParseException ex) {
        StringBuffer str = new StringBuffer();

        String systemId = ex.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1)
                systemId = systemId.substring(index + 1);
            str.append(systemId);
        }
        str.append(':');
        str.append(ex.getLineNumber());
        str.append(':');
        str.append(ex.getColumnNumber());

        return str.toString();

    } // getLocationString(SAXParseException):String

    static Category logger = Category.getInstance(SimpleErrorHandler.class.getName());
 }

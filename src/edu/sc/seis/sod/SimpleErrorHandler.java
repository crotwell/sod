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

		    /** Warning. */
    public void warning(SAXParseException ex) throws SAXException {
	logger.warn(getLocationString(ex), ex);
	setValid(false);
    }

    /** Error. */
    public void error(SAXParseException ex)  throws SAXException {
        if (warmup)
            return;

	logger.error(getLocationString(ex), ex);
	setValid(false);
    }

    /** Fatal error. */
    public void fatalError(SAXParseException ex) throws SAXException {
        if (warmup)
            return;

        logger.fatal(getLocationString(ex), ex);
	setValid(false);
        throw ex;
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

    public void setValid(boolean value) {
	this.valid = value;
    }

    public boolean isValid() {

	return this.valid;
    }

    
    private boolean valid = true;

    static Category logger = Category.getInstance(SimpleErrorHandler.class.getName());
 }

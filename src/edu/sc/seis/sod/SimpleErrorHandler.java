package edu.sc.seis.sod;

import org.apache.log4j.Category;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class SimpleErrorHandler implements ErrorHandler {

    public SimpleErrorHandler(String filename) {
        this.filename = filename;
    }

    public void warning(SAXParseException ex) {
        logger.warn(getLocationString(ex), ex);
    }

    public void error(SAXParseException ex) {
        logger.warn(getLocationString(ex), ex);
    }

    public void fatalError(SAXParseException ex) {
        System.err.println("SOD had trouble loading the strategy file " + filename);
        System.err.println("There appears to be something wrong on line "
                + ex.getLineNumber());
        System.err.println("SOD requires well-formed XML files.  This means, among other things, that every start tag must be matched by an end tag, and that there can be only one root element.  To find out more about what could be wrong with this file, go to xmlIntro.html in the docs in the SOD distribution");
        System.err.println(ex.getLocalizedMessage());
        System.exit(1);
    }

    private String getLocationString(SAXParseException ex) {
        StringBuffer str = new StringBuffer();
        String systemId = ex.getSystemId();
        if(systemId != null) {
            int index = systemId.lastIndexOf('/');
            if(index != -1) systemId = systemId.substring(index + 1);
            str.append(systemId);
            str.append(':');
        }
        str.append(ex.getLineNumber());
        str.append(':');
        str.append(ex.getColumnNumber());
        return str.toString();
    } // getLocationString(SAXParseException):String

    private String filename;

    private static Category logger = Category.getInstance(SimpleErrorHandler.class.getName());
}
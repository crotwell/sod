package edu.sc.seis.sod.validator;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.SinglePropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class Validator{
    public static boolean validate(InputSource in)
        throws IOException, SAXException{
        return validate(in, true);
    }

    public static boolean validate(InputSource in, boolean verbose)
        throws IOException, SAXException{
        synchronized(validationLock){
            if(driver == null ||
               Validator.verbose != verbose) createDriver(verbose);
            try {
                return driver.validate(in);
            } catch (SAXException e) {
                throw new SAXException(xmlFileMalformed, e);
            } catch (IOException e) {
                logger.debug(xmlFileReadingError);
                throw e;
            }
        }
    }

    private static String xmlFileMalformed =
        "The xml file being validated has an error in it";

    private static String xmlFileReadingError =
        "An I/O Error occured reading in the xml file to be validated";

    private static void createDriver(boolean verbose)
        throws IOException, SAXException{
        PropertyMap pm = PropertyMap.EMPTY;//new SinglePropertyMap(ValidateProperty.ERROR_HANDLER,
                                            //   sodEH);
        if(!verbose){
            pm = new SinglePropertyMap(ValidateProperty.ERROR_HANDLER, quietEH);
        }
        Validator.verbose = verbose;
        driver = new ValidationDriver(pm);
        try{
            driver.loadSchema(getSchemaSource());
        }catch(IOException e){
            throw new IOException("Trouble finding the schema for validation");
        }catch(SAXException e){
            throw new SAXException("The loaded schema is not well formed", e);
        }
    }

    private static InputSource getSchemaSource() {
        ClassLoader loader = (Validator.class).getClassLoader();
        return new InputSource(loader.getResourceAsStream(schemaLoc));
    }

    public static void setSchemaLoc(String loc) { schemaLoc = loc; }

    private static ErrorHandler quietEH = new ErrorHandler(){
        public void warning(SAXParseException exception){}

        public void error(SAXParseException exception){}

        public void fatalError(SAXParseException exception){}
    };

    private static ErrorHandler sodEH = new ErrorHandler(){
        public void error(SAXParseException exception){
            handle(exception);
        }

        public void fatalError(SAXParseException exception) {
            handle(exception);
        }

        public void warning(SAXParseException exception) {
            handle(exception);
        }

        private void handle(SAXParseException e){
            System.out.println("The config file being used by Sod has an error at line " + e.getLineNumber() + " at column " + e.getColumnNumber() + " at the identifier " + e.getPublicId());
        }
    };

    private static boolean verbose;

    private static Object validationLock = new Object();

    private static String schemaLoc = "edu/sc/seis/sod/data/sod.rng";

    private static ValidationDriver driver;

    private static Logger logger = Logger.getLogger(Validator.class);
}

package edu.sc.seis.sod.validator;

import java.io.IOException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.SinglePropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public class Validator {

    public Validator(String schemaLoc) {
        this.schemaLoc = schemaLoc;
    }
    
    public String getErrorMessage(){ return errorMessage; }

    public boolean validate(InputSource in) throws IOException, SAXException {
        return validate(in, true);
    }

    public boolean validate(InputSource in, boolean verbose)
            throws IOException, SAXException {
        if(verbose) {
            if(verboseDriver == null) {
                verboseDriver = createDriver(true);
            }
            synchronized(verboseDriver) {
                return verboseDriver.validate(in);
            }
        } else {
            if(driver == null) {
                driver = createDriver(false);
            }
            synchronized(driver) {
                return driver.validate(in);
            }
        }
    }

    private ValidationDriver createDriver(boolean verbose) {
        PropertyMap pm = null;
        if(!verbose) {
            pm = new SinglePropertyMap(ValidateProperty.ERROR_HANDLER, quietEH);
        } else {
            pm = new SinglePropertyMap(ValidateProperty.ERROR_HANDLER,
                                       verboseEH);
        }
        ValidationDriver newDriver = new ValidationDriver(pm);
        try {
            newDriver.loadSchema(getSchemaSource());
        } catch(IOException e) {
            GlobalExceptionHandler.handle("Trouble loading the schema for validation.  Exiting.",
                                          e);
            System.exit(1);
        } catch(SAXException e) {
            GlobalExceptionHandler.handle("The loaded schema is not well formed.  Exiting.",
                                          e);
            System.exit(1);
        }
        return newDriver;
    }

    private InputSource getSchemaSource() {
        ClassLoader loader = (Validator.class).getClassLoader();
        return new InputSource(loader.getResourceAsStream(schemaLoc));
    }

    private ErrorHandler quietEH = new ErrorHandler() {

        public void warning(SAXParseException exception) {}

        public void error(SAXParseException exception) {}

        public void fatalError(SAXParseException exception) {}
    };

    private ErrorHandler verboseEH = new ErrorHandler() {

        public void warning(SAXParseException exception) {
            handle(exception);
        }

        public void error(SAXParseException exception) {
            handle(exception);
        }

        public void fatalError(SAXParseException exception) {
            handle(exception);
        }

        private void handle(SAXParseException ex) {
            errorMessage = "The strategy file is invalid\n";
            errorMessage += "It appears there's something wrong on line "
                    + ex.getLineNumber() + "\n";
            errorMessage += "SOD requires strategy files that conform to a set structure.  See the tactic guide in the docs that came with SOD for help with this.\n";
            errorMessage += "The strategy validator says: " + ex.getLocalizedMessage();
            errorMessage += "If you think SOD will be ok with this strategy file, just wait 10 seconds.  SOD will start anyway then...";
        }
    };

    private String errorMessage;
    
    private String schemaLoc = "edu/sc/seis/sod/data/sod.rng";

    public static final String SOD_SCHEMA_LOC = "edu/sc/seis/sod/data/sod.rng";

    private ValidationDriver verboseDriver, driver;
}
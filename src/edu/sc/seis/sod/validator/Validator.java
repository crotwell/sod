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
	public Validator(String schemaLoc) {
		this.schemaLoc = schemaLoc;
	}
    public  boolean validate(InputSource in)
		throws IOException, SAXException{
		return validate(in, true);
    }
	
    public  boolean validate(InputSource in, boolean verbose)
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
	
    private void createDriver(boolean verbose)
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
	
    private InputSource getSchemaSource() {
		ClassLoader loader = (Validator.class).getClassLoader();
		return new InputSource(loader.getResourceAsStream(schemaLoc));
    }
	
    private ErrorHandler quietEH = new ErrorHandler(){
		public void warning(SAXParseException exception){}
		
		public void error(SAXParseException exception){}
		
		public void fatalError(SAXParseException exception){}
    };
	
	
    private static boolean verbose;
	
    private Object validationLock = new Object();
	
    private  String schemaLoc = "edu/sc/seis/sod/data/sod.rng";
	
	public static final String SOD_SCHEMA_LOC = "edu/sc/seis/sod/data/sod.rng";
	
    private  ValidationDriver driver;
	
    private static Logger logger = Logger.getLogger(Validator.class);
}

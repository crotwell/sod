package edu.sc.seis.sod.validator;

import java.io.IOException;

import junit.framework.TestCase;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ValidatorTest extends TestCase{
    public ValidatorTest(String name){
        super(name);
		validator = new Validator("edu/sc/seis/sod/data/validator/simpleSchema.rng");
    }
    
    public void testInvalid() throws SAXException, IOException{
        assertFalse(validator.validate(getIS(invalid), false));
    }
    
    public void testValid() throws SAXException, IOException{
        assertTrue(validator.validate(getIS(valid), false));
    }
    
    public void testMalformed() throws IOException{
        try {
            validator.validate(getIS(malformed), false);
            assertTrue("Validate should throw a SAXException on malformed data", false);
        } catch (SAXException e) {
            assertTrue(true);
        }
    }
    
    private InputSource getIS(String jarLoc){
        return new InputSource(loader.getResourceAsStream(jarLoc));
    }
    
    private ClassLoader loader = this.getClass().getClassLoader();
    
    private String malformed = "edu/sc/seis/sod/data/validator/malformed.xml";
    
    private String valid = "edu/sc/seis/sod/data/validator/valid.xml";
    
    private String invalid = "edu/sc/seis/sod/data/validator/invalid.xml";
	
	private Validator validator;
}

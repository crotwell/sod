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
    
    public void testSimpleSod() throws IOException, SAXException {
        Validator v = new Validator("edu/sc/seis/sod/data/relax/sod.rng");
        v.validate(getIS(simpleSod), true);
        
    }
    
    private InputSource getIS(String jarLoc){
        InputSource inSource = new InputSource(loader.getResourceAsStream(jarLoc));
        inSource.setSystemId(loader.getResource(jarLoc).toString());
        return inSource;
    }
    
    private ClassLoader loader = this.getClass().getClassLoader();

    private String malformed = "edu/sc/seis/sod/data/validator/malformed.xml";
    
    private String simpleSod = "edu/sc/seis/sod/data/validator/simpleSod.xml";
    
    private String valid = "edu/sc/seis/sod/data/validator/valid.xml";
    
    private String invalid = "edu/sc/seis/sod/data/validator/invalid.xml";
	
	private Validator validator;
}

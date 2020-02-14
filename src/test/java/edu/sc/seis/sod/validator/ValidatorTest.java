package edu.sc.seis.sod.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ValidatorTest {
	
	@BeforeEach
    public void setup( ){
		validator = new Validator("edu/sc/seis/sod/data/validator/simpleSchema.rng");
    }
    
	@Test
    public void testInvalid() throws SAXException, IOException{
        assertFalse(validator.validate(getIS(invalid), false));
    }

	@Test
    public void testValid() throws SAXException, IOException{
        assertTrue(validator.validate(getIS(valid), false));
    }

	@Test
    public void testMalformed() throws IOException{
        try {
            validator.validate(getIS(malformed), false);
            assertTrue(false, "Validate should throw a SAXException on malformed data");
        } catch (SAXException e) {
            assertTrue(true);
        }
    }

	@Test
    public void testSimpleSod() throws IOException, SAXException {
        Validator v = new Validator();
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

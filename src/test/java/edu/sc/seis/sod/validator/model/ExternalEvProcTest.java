/**
 * ExternalEvProc.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.Test;

import edu.sc.seis.sod.validator.tour.FormPrinter;


public class ExternalEvProcTest {
	
	@Test
    public void testExternalRef() throws XMLStreamException, IOException{
        StAXModelBuilder modBuild = new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/externalEvProc.rng");
        modBuild.getRoot().accept(new FormPrinter(8));
    }
}


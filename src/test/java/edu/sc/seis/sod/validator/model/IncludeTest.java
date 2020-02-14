/**
 * IncludeTest.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.Test;

public class IncludeTest {
	
	@Test
    public void testCardinality() throws IOException, XMLStreamException{
        StAXModelBuilder modBuild = new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/include.rng");
        //modBuild.getRoot().accept(new FormPrinter(8));
        MostBasicTest.checkMostBasicStructure(modBuild.getRoot());
    }
}

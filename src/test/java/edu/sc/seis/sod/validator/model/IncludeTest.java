/**
 * IncludeTest.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

public class IncludeTest extends TestCase{
    public void testCardinality() throws IOException, XMLStreamException{
        StAXModelBuilder modBuild = new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/include.rng");
        //modBuild.getRoot().accept(new FormPrinter(8));
        MostBasicTest.checkMostBasicStructure(modBuild.getRoot());
    }
}

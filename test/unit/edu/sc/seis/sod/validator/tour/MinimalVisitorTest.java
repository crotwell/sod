/*
 * Created on Jul 13, 2004
 */
package edu.sc.seis.sod.validator.tour;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;
import edu.sc.seis.sod.validator.example.ExampleBuilder;
import edu.sc.seis.sod.validator.model.StAXModelBuilder;

/**
 * @author Charlie Groves
 */
public class MinimalVisitorTest extends TestCase {
    public void testComparisonToExampleBuilder() throws XMLStreamException,
            IOException {
        compareToExampleBuilder("jar:edu/sc/seis/sod/data/validator/mostBasic.rng");
        compareToExampleBuilder("jar:edu/sc/seis/sod/data/validator/choiceMulti.rng");
        compareToExampleBuilder("jar:edu/sc/seis/sod/data/validator/data.rng");
        compareToExampleBuilder("jar:edu/sc/seis/sod/data/validator/attributes.rng");
    }
    
    public void testXMLDelimiting() throws XMLStreamException, IOException {
        compareToExampleBuilder("jar:edu/sc/seis/sod/data/validator/mostBasic.rng", new XMLWritingTourist(true), new ExampleBuilder(true), false);
        compareToExampleBuilder("jar:edu/sc/seis/sod/data/validator/choiceMulti.rng", new XMLWritingTourist(true), new ExampleBuilder(true), false);
        compareToExampleBuilder("jar:edu/sc/seis/sod/data/validator/data.rng", new XMLWritingTourist(true), new ExampleBuilder(true), false);
        compareToExampleBuilder("jar:edu/sc/seis/sod/data/validator/attributes.rng", new XMLWritingTourist(true), new ExampleBuilder(true), false);
    }

    public static void compareToExampleBuilder(String schemaLoc) throws XMLStreamException, IOException {
        compareToExampleBuilder(schemaLoc, new XMLWritingTourist(), new ExampleBuilder(false), false);
    }
    
    public static void compareToExampleBuilder(String schemaLoc, XMLWritingTourist visitor, ExampleBuilder builder, boolean verbose)
            throws XMLStreamException, IOException {
        StAXModelBuilder modBuild = new StAXModelBuilder(schemaLoc);
        builder.write(modBuild.getRoot());
        String exampleBuilderResult = builder.toString();
        MinimalVisitGuide impatient = new MinimalVisitGuide(modBuild.getRoot());
        impatient.lead(visitor);
        if (verbose) {
            System.out.println(exampleBuilderResult);
            System.out.println(visitor.getResult());
        }
        assertEquals(builder.toString(), visitor.getResult());
    }
}
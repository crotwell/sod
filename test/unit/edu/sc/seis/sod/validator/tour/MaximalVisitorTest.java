/*
 * Created on Jul 13, 2004
 */
package edu.sc.seis.sod.validator.tour;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;
import edu.sc.seis.sod.validator.model.*;

/**
 * @author Charlie Groves
 */
public class MaximalVisitorTest extends TestCase {

    public void testCardinalityVisitation() throws XMLStreamException,
            IOException {
        runVisitation("jar:edu/sc/seis/sod/data/validator/cardinality.rng",
                new String[] { "baseElement", "optionalEl", "oneOrMore",
                        "zeroOrMore", "thisOne", "thenThisOne", "thenAnotherOne",
                        "finallyThisOne" });
    }

    public void testMultiChoiceVisitation() throws XMLStreamException,
            IOException {
        runVisitation("jar:edu/sc/seis/sod/data/validator/choiceMulti.rng",
                new String[] { "baseElement", "internalEl1", "internalEl2" });
    }

    public void testSelfRef() throws XMLStreamException,
            IOException {
        runVisitation("jar:edu/sc/seis/sod/data/validator/selfReferential.rng",
                new String[] { "internalEl", "internalEl" });
    }

    private void runVisitation(String schema, String[] visitOrder)
            throws XMLStreamException, IOException {
        StAXModelBuilder modBuild = new StAXModelBuilder(schema);
        MaximalVisitGuide patient = new MaximalVisitGuide(
                modBuild.getRoot());
        MaximalTourist tourist = new MaximalTourist(visitOrder);
        patient.lead(tourist);
        tourist.finish();
    }

    private class MaximalTourist implements Tourist {

        public MaximalTourist(String[] visitOrder) {
            this.visitOrder = visitOrder;
        }

        public void finish() {
            assertEquals(visitOrder.length, i);
        }

        public void visit(NamedElement ne) {
            assertEquals(visitOrder[i++], ne.getName());
        }

        private String[] visitOrder;

        private int i = 0;

        public void visit(Attribute attr) {}

        public void leave(Attribute attr) {}

        public void visit(Choice choice) {}

        public void leave(Choice choice) {}

        public void visit(Data d) {}

        public void visit(Empty e) {}

        public void visit(Group g) {}

        public void leave(Group g) {}

        public void visit(Interleave i) {}

        public void leave(Interleave i) {}

        public void leave(NamedElement ne) {}

        public void visit(Text t) {}

        public void visit(Value v) {}

        public void visit(NotAllowed na) {}
    }
}
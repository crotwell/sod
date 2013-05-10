/**
 * CardinalityTest.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

public class CardinalityTest extends TestCase{

    public void testCardinality() throws IOException, XMLStreamException{
        StAXModelBuilder modBuild = new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/cardinality.rng");
        //modBuild.getRoot().accept(new FormPrinter(8));

        assertTrue(modBuild.getRoot() instanceof NamedElement);
        NamedElement nameElRoot = (NamedElement)modBuild.getRoot();
        assertEquals("baseElement", nameElRoot.getName());

        assertTrue(nameElRoot.getChild() instanceof Group);
        Group internalGroup = (Group)nameElRoot.getChild();
        Form[] groupKids = internalGroup.getChildren();
        assertEquals(4, groupKids.length);

        assertTrue(groupKids[0] instanceof NamedElement);
        NamedElement optionalEl = (NamedElement)groupKids[0];
        assertEquals(0, optionalEl.getMin());
        assertEquals(1, optionalEl.getMax());

        assertTrue(groupKids[1] instanceof NamedElement);
        NamedElement oneOrMoreEl = (NamedElement)groupKids[1];
        assertEquals(1, oneOrMoreEl.getMin());
        assertEquals(Integer.MAX_VALUE, oneOrMoreEl.getMax());

        assertTrue(groupKids[2] instanceof NamedElement);
        NamedElement zeroOrMoreEl = (NamedElement)groupKids[2];
        assertEquals(0, zeroOrMoreEl.getMin());
        assertEquals(Integer.MAX_VALUE, zeroOrMoreEl.getMax());
    }
}


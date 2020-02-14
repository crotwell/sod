/**
 * MultigenTest.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.Test;

public class MultigenTest  {

	@Test
    public void testMultiChild() throws IOException, XMLStreamException{
        StAXModelBuilder modBuild = new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/multiChild.rng");
        //modBuild.getRoot().accept(new FormPrinter(8));
        assertTrue(modBuild.getRoot() instanceof NamedElement);
        NamedElement nameElRoot = (NamedElement)modBuild.getRoot();
        assertEquals("baseElement", nameElRoot.getName());

        assertTrue(nameElRoot.getChild() instanceof Group);
        Group internalGroup = (Group)nameElRoot.getChild();
        Form[] groupKids = internalGroup.getChildren();
        assertEquals(2, groupKids.length);
    }

	@Test
    public void testChoiceMulti() throws IOException, XMLStreamException{
        StAXModelBuilder modBuild = new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/choiceMulti.rng");
        assertTrue(modBuild.getRoot() instanceof NamedElement);
        NamedElement nameElRoot = (NamedElement)modBuild.getRoot();
        assertEquals("baseElement", nameElRoot.getName());

        assertTrue(nameElRoot.getChild() instanceof Choice);
        Choice internalGroup = (Choice)nameElRoot.getChild();
        assertTrue(internalGroup.getMax() > 1);
        Form[] groupKids = internalGroup.getChildren();
        assertEquals(2, groupKids.length);
    }
}


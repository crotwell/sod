/**
 * MultigenTest.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;
import edu.sc.seis.sod.validator.model.*;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import junit.framework.TestCase;

public class MultigenTest extends TestCase {

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

    public void testChoiceMulti() throws IOException, XMLStreamException{
        StAXModelBuilder modBuild = new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/choiceMulti.rng");
        //modBuild.getRoot().accept(new FormPrinter(8));
        assertTrue(modBuild.getRoot() instanceof NamedElement);
        NamedElement nameElRoot = (NamedElement)modBuild.getRoot();
        assertEquals("baseElement", nameElRoot.getName());

        assertTrue(nameElRoot.getChild() instanceof Choice);
        Choice internalGroup = (Choice)nameElRoot.getChild();
        Form[] groupKids = internalGroup.getChildren();
        assertEquals(2, groupKids.length);
    }
}


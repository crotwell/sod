/**
 * ParentTest.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.Test;

public class ParentTest {
	
	@Test
    public void testParent() throws XMLStreamException, IOException{
        StAXModelBuilder modBuild = new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/externalRef.rng");
        //modBuild.getRoot().accept(new FormPrinter(8));
        assertTrue(modBuild.getRoot() instanceof NamedElement);
        NamedElement wrapper = (NamedElement)modBuild.getRoot();
        assertTrue(wrapper.getChild() instanceof Group);
        Group child = (Group)wrapper.getChild();
        assertEquals(wrapper, child.getParent());
        Form[] groupKids = child.getChildren();
        for (int i = 0; i < groupKids.length; i++) {
            assertEquals(child, groupKids[i].getParent());
        }
    }
}

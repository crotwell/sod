/**
 * ParentTest.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

public class ParentTest extends TestCase{
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

/**
 * ExternalRefTest.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;



public class ExternalRefTest extends TestCase{
    public void testExternalRef() throws XMLStreamException, IOException{
        StAXModelBuilder modBuild = new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/externalRef.rng");
        //modBuild.getRoot().accept(new FormPrinter(8));
        assertTrue(modBuild.getRoot() instanceof NamedElement);
        NamedElement wrapper = (NamedElement)modBuild.getRoot();
        assertTrue(wrapper.getChild() instanceof Group);
        Group child = (Group)wrapper.getChild();
        MostBasicTest.checkMostBasicStructure(child.getChildren()[0]);
        MostBasicTest.checkMostBasicStructure(child.getChildren()[1]);
    }
}


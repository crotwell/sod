/**
 * SelfRefTest.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;
import edu.sc.seis.sod.validator.model.*;

import edu.sc.seis.sod.validator.ModelWalker;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import junit.framework.TestCase;

public class SelfRefTest extends TestCase {

    public void testSelfReferential() throws IOException, XMLStreamException{
        StAXModelBuilder modBuild = new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/selfReferential.rng");
        //modBuild.getRoot().accept(new FormPrinter(8));
        assertTrue(modBuild.getRoot() instanceof NamedElement);
        NamedElement nameElRoot = (NamedElement)modBuild.getRoot();
        assertEquals("baseElement", nameElRoot.getName());
        assertTrue(nameElRoot.getChild() instanceof Group);
        Group suppliedGroup = (Group)nameElRoot.getChild();
        assertEquals(2, suppliedGroup.getChildren().length);

        Form[] groupKids = suppliedGroup.getChildren();
        assertTrue(groupKids[0] instanceof NamedElement);
        assertFalse(groupKids[0].isFromDef());

        assertTrue(groupKids[1].isFromDef());
        assertEquals(groupKids[1].getDef().getName(), "selfRef");
        assertTrue(ModelWalker.isSelfReferential(groupKids[1]));
    }
}


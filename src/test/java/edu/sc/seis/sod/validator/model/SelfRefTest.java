/**
 * SelfRefTest.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.Test;

import edu.sc.seis.sod.validator.ModelWalker;

public class SelfRefTest  {

	@Test
    public void testSelfReferential() throws IOException, XMLStreamException{
        StAXModelBuilder modBuild = new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/selfReferential.rng");
        //modBuild.getRoot().accept(new FormPrinter(8));
        assertTrue(modBuild.getRoot() instanceof Group);
        Group rootGroup = (Group)modBuild.getRoot();
        assertEquals(2, rootGroup.getChildren().length);

        Form[] groupKids = rootGroup.getChildren();
        assertTrue(groupKids[0] instanceof NamedElement);
        assertFalse(groupKids[0].isFromDef());

        assertTrue(groupKids[1].isFromDef());
        assertEquals(groupKids[1].getDef().getName(), "selfRef");
        assertTrue(ModelWalker.isSelfReferential(groupKids[1]));
    }
}


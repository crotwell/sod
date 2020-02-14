/**
 * MostBasicTest.java
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

public class MostBasicTest {
	
	@Test
    public void testMostBasic() throws IOException, XMLStreamException{
        StAXModelBuilder modBuild = new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/mostBasic.rng");
        //modBuild.getRoot().accept(new FormPrinter(8));
        checkMostBasicStructure(modBuild.getRoot());
    }

	@Test
    public static void checkMostBasicStructure(Form mostBasicRoot){
        assertTrue(mostBasicRoot instanceof NamedElement);
        NamedElement nameElRoot = (NamedElement)mostBasicRoot;
        assertEquals("mustHaveThisElement", nameElRoot.getName());
        checkMostBasicDefStructure(nameElRoot.getChild());
    }

	@Test
    public static void checkMostBasicDefStructure(Form mostBasicDef){

        assertTrue(mostBasicDef instanceof NamedElement);
        NamedElement nameElChild = (NamedElement)mostBasicDef;
        assertEquals("withThisElementInsideOfIt", nameElChild.getName());

        assertTrue(nameElChild.isFromDef());
        assertEquals(nameElChild.getDef().getName(), "internalEl");
        assertFalse(ModelWalker.isSelfReferential(nameElChild));

        assertTrue(nameElChild.getChild() instanceof Empty);

    }
}

/**
 * MostBasicTest.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;
import edu.sc.seis.sod.validator.model.*;

import edu.sc.seis.sod.validator.ModelWalker;
import edu.sc.seis.sod.validator.model.StAXModelBuilder;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import junit.framework.TestCase;

public class MostBasicTest extends TestCase{
    public void testMostBasic() throws IOException, XMLStreamException{
        StAXModelBuilder modBuild = new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/mostBasic.rng");
        //modBuild.getRoot().accept(new FormPrinter(8));
        checkMostBasicStructure(modBuild.getRoot());
    }

    public static void checkMostBasicStructure(Form mostBasicRoot){
        assertTrue(mostBasicRoot instanceof NamedElement);
        NamedElement nameElRoot = (NamedElement)mostBasicRoot;
        assertEquals("mustHaveThisElement", nameElRoot.getName());
        checkMostBasicDefStructure(nameElRoot.getChild());
    }

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

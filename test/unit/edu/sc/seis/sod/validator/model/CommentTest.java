/**
 * CommentTest.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import junit.framework.TestCase;

public class CommentTest extends TestCase{
    public void testCommentedDocument() throws XMLStreamException, IOException{
        StAXModelBuilder modBuild = new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/commented.rng");
        //modBuild.getRoot().accept(new FormPrinter(8));
        MostBasicTest.checkMostBasicStructure(modBuild.getRoot());
    }
}

/**
 * AnnotationTest.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;
import edu.sc.seis.sod.validator.model.*;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import junit.framework.TestCase;

public class AnnotationTest extends TestCase{
    public void testMostBasic() throws IOException, XMLStreamException{
        StAXModelBuilder modBuild = new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/annotation.rng");
        modBuild.getRoot().accept(new FormPrinter(8));
    }

}


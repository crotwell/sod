/**
 * AnnotationTest.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import junit.framework.TestCase;

public class AnnotationTest extends TestCase{
    public void testAnnotationAssignment() throws IOException, XMLStreamException{
        StAXModelBuilder modBuild = new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/annotation.rng");
        NamedElement base = (NamedElement)modBuild.getRoot();
        assertEquals(base.getName(), base.getAnnotation().getSummary());
        Group baseChild = (Group)base.getChild();
        Form[] baseChildren = baseChild.getChildren();
        for (int i = 0; i < baseChildren.length; i++) {
            handle(baseChildren[i]);
        }
    }

    private static void handle(Form f){
        if(f instanceof NamedElement){
            NamedElement cur = (NamedElement)f;
            assertEquals(cur.getName(), cur.getAnnotation().getSummary());
        }else if(f instanceof Choice){
            assertEquals("choice", f.getAnnotation().getSummary());
        }else if(f instanceof Data){
            assertTrue(f.getAnnotation().getInclude());
        }
    }

    public void testAnnotationOnCardinality() throws IOException, XMLStreamException{
        try{
            new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/wrongAnnotation.rng");
            assertTrue("Should've thrown a runtime exception when it encountered an Annotation in a cardinality operator", false);
        }catch(RuntimeException e){
            assertTrue(true);
        }
    }
}


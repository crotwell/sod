package edu.sc.seis.sod.validator;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;
import edu.sc.seis.sod.validator.model.Choice;
import edu.sc.seis.sod.validator.model.Form;
import edu.sc.seis.sod.validator.model.NamedElement;
import edu.sc.seis.sod.validator.model.StAXModelBuilder;

/**
 * @author oliverpa Created on Sep 2, 2004
 */
public class ModelWalkerTest extends TestCase {

    private StAXModelBuilder modBuild;

    public ModelWalkerTest(String name) throws XMLStreamException, IOException {
        super(name);
        modBuild = new StAXModelBuilder("jar:edu/sc/seis/sod/data/validator/selfRefChoice.rng");
    }

    public void testZeroDistance(){
        NamedElement base = (NamedElement)modBuild.getRoot();
        assertEquals(0, ModelWalker.getDistance(base, base));
    }

    public void testOneDistance() {
        NamedElement base = (NamedElement)modBuild.getRoot();
        NamedElement sub = getChildFromNamedElementWithChoice(base, "sub");
        assertEquals(1, ModelWalker.getDistance(base, sub));
    }

    public void testSelfReferentialStructure() {
        NamedElement base = (NamedElement)modBuild.getRoot();
        Choice baseChoice = (Choice)base.getChild();
        assertEquals(2, baseChoice.getChildren().length);
        NamedElement subBase = (NamedElement)baseChoice.getChildren()[0];
        assertEquals(base.getDef(), subBase.getDef());
        assertEquals("base", subBase.getName());
        Choice subBaseChoice = (Choice)subBase.getChild();
        assertEquals(2, subBaseChoice.getChildren().length);
    }

    public void testTwoDistance() {
        NamedElement base = (NamedElement)modBuild.getRoot();
        NamedElement sub = getChildFromNamedElementWithChoice(base, "sub");
        NamedElement gusgus = (NamedElement)sub.getChild();
        assertEquals(2, ModelWalker.getDistance(base, gusgus));
    }

    private static NamedElement getChildFromNamedElementWithChoice(NamedElement parent,
                                                                   String childName) {
        Choice c = (Choice)parent.getChild();
        Form[] kids = c.getChildren();
        for(int i = 0; i < kids.length; i++) {
            NamedElement cur = (NamedElement)kids[i];
            if(cur.getName().equals(childName)) { return cur; }
        }
        return null;
    }
}
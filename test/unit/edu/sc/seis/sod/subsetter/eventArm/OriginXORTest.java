package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.XMLConfigUtil;
import junit.framework.TestCase;
import org.w3c.dom.Element;

public class OriginXORTest
    extends TestCase
{
    edu.sc.seis.sod.subsetter.origin.OriginXOR originxor = null;

    public OriginXORTest(String name) {
        super(name);
    }

    public edu.sc.seis.sod.subsetter.origin.OriginXOR createInstance() throws Exception {
        Element xor = XMLConfigUtil.parse("<originXOR>"+
                                              "<passOrigin/>"+
                                              "<originNOT><passOrigin/></originNOT>"+
                                              "</originXOR>");
        return new edu.sc.seis.sod.subsetter.origin.OriginXOR(xor);
    }

    protected void setUp() throws Exception {
        super.setUp();
        originxor = createInstance();
    }

    protected void tearDown() throws Exception {
        originxor = null;
        super.tearDown();
    }

    public void testAccept() throws Exception {
        assertTrue(originxor.accept(null, null, null));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(OriginXORTest.class);
    }
}


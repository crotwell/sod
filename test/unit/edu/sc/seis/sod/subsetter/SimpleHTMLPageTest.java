/**
 * TestSimpleHTMLPage.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter;

import java.io.File;
import junit.framework.TestCase;
import java.io.IOException;

public class SimpleHTMLPageTest extends TestCase{
    public SimpleHTMLPageTest(String name){
        super(name);
    }

    public void setUp() throws IOException {
        baseDir = new SimpleHTMLPage("Test", new File("test.html"));
        threeBelowDir = new SimpleHTMLPage("Test2", new File("./" +oneTwoThree + "test2.html"));
        oneAboveDir = new SimpleHTMLPage("Test3", new File("../test3.html"));
    }

    public void testRelativePathTo(){
        assertEquals(oneTwoThree + "test2.html", baseDir.relativePathTo(threeBelowDir));
        assertEquals(dotDotDot + "test.html", threeBelowDir.relativePathTo(baseDir));
        assertEquals("../test3.html", baseDir.relativePathTo(oneAboveDir));
        assertEquals(dotDotDot + "../test3.html", threeBelowDir.relativePathTo(oneAboveDir));
    }

    public void testDotsToCommonBase() throws IOException{
        assertEquals("", baseDir.dotsToCommonBase(baseDir));
        assertEquals("", baseDir.dotsToCommonBase(threeBelowDir));
        assertEquals(dotDotDot, threeBelowDir.dotsToCommonBase(baseDir));
     }

    public void testGetCommonBase() throws IOException{
        assertEquals(0, baseDir.getCommonBaseDistance(baseDir.getDirectory()));
        assertEquals(0, baseDir.getCommonBaseDistance(threeBelowDir.getDirectory()));
        assertEquals(3, threeBelowDir.getCommonBaseDistance(baseDir.getDirectory()));
        assertEquals(0, oneAboveDir.getCommonBaseDistance(baseDir.getDirectory()));
    }

    private String oneTwoThree = "one/two/three/";
    private String dotDotDot = "../../../";
    private SimpleHTMLPage baseDir, threeBelowDir, oneAboveDir;
}

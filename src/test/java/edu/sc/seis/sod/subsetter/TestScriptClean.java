package edu.sc.seis.sod.subsetter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;



public class TestScriptClean extends TestCase {
    
    String beforeLineScript = "   \n"+
"                print networkAttr.get_code()\n"+
"\n"+
"                out = True\n";
    
    String afterLineScript =
    "                print networkAttr.get_code()\n"+
    "\n"+
    "                out = True\n"+
    "        \n"+
    "\n"+
    "\n"+
    "  \n";

    public void testBeforeLineScript() {
        String out = AbstractScriptSubsetter.cleanScript(beforeLineScript);
        int numLines = out.split("\n").length;
        assertEquals("num lines", 3, numLines);
    }
    
    public void testAfterLineScript() {
        String out = AbstractScriptSubsetter.cleanScript(afterLineScript);
        int numLines = out.split("\n").length;
        assertEquals("num lines", 3, numLines);
    }
    
    public void testSimpleRegEx() {
        String s = " \n \n \nabc\n \n \n \nabc\nabc\n";
        Matcher matcher = 
            Pattern.compile("((?:(?: *\\n)*(?: *\\S[^\\n]*\\n))*)", Pattern.DOTALL).matcher(s);
        assertTrue(matcher.matches());
    }

    public void testNumSpacesRegEx() {
        String s = "  abc";
        Matcher matcher = 
            Pattern.compile("( *).*").matcher(s);
        assertTrue(matcher.matches());
        assertEquals("num spaces", 2, matcher.group(1).length());
    }
    public void testNoNumSpacesRegEx() {
        String s = "abc";
        Matcher matcher = 
            Pattern.compile("( *).*").matcher(s);
        assertTrue(matcher.matches());
        assertEquals("num spaces", 0, matcher.group(1).length());
    }
    public void testAllNumSpacesRegEx() {
        String s = "   ";
        Matcher matcher = 
            Pattern.compile("( *).*").matcher(s);
        assertTrue(matcher.matches());
        assertEquals("num spaces", 3, matcher.group(1).length());
    }
    public void testEmptyNumSpacesRegEx() {
        String s = "";
        Matcher matcher = 
            Pattern.compile("( *).*").matcher(s);
        assertTrue(matcher.matches());
        assertEquals("num spaces", 0, matcher.group(1).length());
    }

    public void testZapNumSpacesRegEx() {
        String s = "    abc";
        Matcher matcher = 
            Pattern.compile(" {0,3}(.*)").matcher(s);
        assertTrue(matcher.matches());
        assertEquals("num spaces", " abc", matcher.group(1));
    }
}

package edu.sc.seis.sod.subsetter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import junit.framework.TestCase;



public class TestScriptClean  {
    
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

    @Test
    public void testBeforeLineScript() {
        String out = AbstractScriptSubsetter.cleanScript(beforeLineScript);
        int numLines = out.split("\n").length;
        assertEquals( 3, numLines, "num lines");
    }

    @Test
    public void testAfterLineScript() {
        String out = AbstractScriptSubsetter.cleanScript(afterLineScript);
        int numLines = out.split("\n").length;
        assertEquals( 3, numLines, "num lines");
    }

    @Test
    public void testSimpleRegEx() {
        String s = " \n \n \nabc\n \n \n \nabc\nabc\n";
        Matcher matcher = 
            Pattern.compile("((?:(?: *\\n)*(?: *\\S[^\\n]*\\n))*)", Pattern.DOTALL).matcher(s);
        assertTrue(matcher.matches());
    }

    @Test
    public void testNumSpacesRegEx() {
        String s = "  abc";
        Matcher matcher = 
            Pattern.compile("( *).*").matcher(s);
        assertTrue(matcher.matches());
        assertEquals( 2, matcher.group(1).length(), "num spaces");
    }
    @Test
    public void testNoNumSpacesRegEx() {
        String s = "abc";
        Matcher matcher = 
            Pattern.compile("( *).*").matcher(s);
        assertTrue(matcher.matches());
        assertEquals( 0, matcher.group(1).length(), "num spaces");
    }
    @Test
    public void testAllNumSpacesRegEx() {
        String s = "   ";
        Matcher matcher = 
            Pattern.compile("( *).*").matcher(s);
        assertTrue(matcher.matches());
        assertEquals( 3, matcher.group(1).length(), "num spaces");
    }
    @Test
    public void testEmptyNumSpacesRegEx() {
        String s = "";
        Matcher matcher = 
            Pattern.compile("( *).*").matcher(s);
        assertTrue(matcher.matches());
        assertEquals( 0, matcher.group(1).length(), "num spaces");
    }

    @Test
    public void testZapNumSpacesRegEx() {
        String s = "    abc";
        Matcher matcher = 
            Pattern.compile(" {0,3}(.*)").matcher(s);
        assertTrue(matcher.matches());
        assertEquals( " abc", matcher.group(1), "num spaces");
    }
}

/**
 * TabularSectionTest.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter;

import junit.framework.TestCase;

public class TabularSectionTest extends TestCase{
    public TabularSectionTest(String name){
        super(name);
    }
    
    public void setUp(){
        sec = new TabularSection("Musicians", columns);
    }
    
    public void testAdd(){
        fillTable();
        assertEquals(fullTable, sec.getSection());
    }
    
    public void testWrongSizeAppend(){
        try{
            String[] strings = {};
            sec.append("First", strings);
            fail();
        }catch(IllegalArgumentException e){
            assertTrue(true);
        }
    }
    
    public void testUpdate(){
        fillTable();
        String[] newValues = { "The Broadways", null };
        sec.append("First", newValues);
        assertEquals(updatedTable, sec.getSection());
    }
    
    private void fillTable(){
        String[] firstValues = { "Belle & Sebastian", "Michael Bolton" };
        sec.append("First", firstValues);
        String[] secondValues = { "Bentley Rhythm Ace", "Mariah Carey" };
        sec.append("Second", secondValues);
    }
    
    public void testEmpty(){ assertEquals(emptyTable, sec.getSection()); }
    
    public void testGetTitle(){
        assertEquals(title, sec.getTitle());
    }
    
    private String[] columns = { "Cool", "Sad" };
    
    private TabularSection sec;
    
    //Shamelessly taken from http://vzone.virgin.net/sizzling.jalfrezi/iniframe.htm
    //as this example amused me
    private String title =
        "  <TR>\n" +
        "    <TH>Musicians</TH>\n" +
        "  </TR>\n";
    
    private String header =
        "  <TR>\n" +
        "    <TH ALIGN=\"LEFT\">Cool</TH>\n" +
        "    <TH ALIGN=\"LEFT\">Sad</TH>\n" +
        "  </TR>\n";
        
        private String emptyTable =
        "<TABLE>\n" +
        title +
        header +
        "</TABLE>\n";
    
    private String fullTable =
        "<TABLE>\n" +
        title +
        header +
        "  <TR>\n" +
        "    <TD>Belle & Sebastian</TD>\n" +
        "    <TD>Michael Bolton</TD>\n" +
        "  </TR>\n" +
        "  <TR>\n" +
        "    <TD>Bentley Rhythm Ace</TD>\n" +
        "    <TD>Mariah Carey</TD>\n" +
        "  </TR>\n" +
        "</TABLE>\n";
    
    private String updatedTable =
        "<TABLE>\n" +
        title +
        header +
        "  <TR>\n" +
        "    <TD>The Broadways</TD>\n" +
        "    <TD>Michael Bolton</TD>\n" +
        "  </TR>\n" +
        "  <TR>\n" +
        "    <TD>Bentley Rhythm Ace</TD>\n" +
        "    <TD>Mariah Carey</TD>\n" +
        "  </TR>\n" +
        "</TABLE>\n";
}

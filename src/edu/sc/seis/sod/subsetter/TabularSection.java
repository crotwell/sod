/**
 * TabularPageSection.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TabularSection extends PageSection{
    public TabularSection(String name, String[] columns){
        super(name);
        this.columns = columns;
        header = new TableRow(true, columns);
    }
    
    public String getSection(){
        StringBuffer sec = new StringBuffer("<TABLE>\n");
        sec.append(getTitle());
        sec.append(header.getRow());
        Iterator it = rows.keySet().iterator();
        while(it.hasNext()){
            sec.append(((TableRow)rows.get(it.next())).getRow());
        }
        sec.append("</TABLE>\n");
        return sec.toString();
    }
    
    public String getTitle() {
        return "  <TR>\n" +
            "    <TH>" + getName() + "</TH>\n" +
            "  </TR>\n";
    }
    
    /**
     * after this call, there will be a row named rowName with the values in values
     * if the row doesn't exist.  If the row for rowName already existed, only non-null
     * items in the values[] will overwrite existing values
     */
    public void append(String rowName, String[] values){
        if(values != null && values.length != columns.length){
            throw new IllegalArgumentException("the number of values to append to the table must be the same as the numer of columns");
        }
        if(!rows.containsKey(rowName)){
            rows.put(rowName, new TableRow(false, values));
        }else{
            TableRow row = (TableRow)rows.get(rowName);
            row.updateValues(values);
        }
    }
    
    private class TableRow{
        public TableRow(boolean header, String[] columnValues){
            if(header){
                tag = "<TH ALIGN=\"LEFT\">";
                cTag = "</TH>";
            }
            this.columnValues = columnValues;
        }
        
        public void updateValues(String[] values) {
            for (int i = 0; i < values.length; i++) {
                if(values[i] != null) columnValues[i] = values[i];
            }
        }
        
        public String getRow(){
            StringBuffer row = new StringBuffer(TWO + "<TR>\n");
            for (int i = 0; i < columnValues.length; i++) {
                if(columnValues[i] == null) row.append(FOUR + tag + "&nbsp;" + cTag + "\n");
                else row.append(FOUR + tag + columnValues[i] + cTag + "\n");
            }
            row.append(TWO + "</TR>\n");
            return row.toString();
        }
        
        public void setValue(int column, String value){
            columnValues[column] = value;
        }
        
        private String tag = "<TD>";
        private String cTag = "</TD>";
        private String[] columnValues;
    }
    
    private static final String SIX = "      ";
    
    private static final String FOUR = "    ";
    
    private static final String TWO = "  ";
    
    private TableRow header;
    
    private Map rows = new HashMap();
    
    private String[] columns;
}

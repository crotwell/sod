/**
 * PageSection.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PageSection{
    public PageSection(String name){
        this(name, "");
    }
    
    public PageSection(String name, String text){
        this.name = name;
        append(text);
    }
    
    public void clear(){ contents = new StringBuffer(""); }
    
    public void append(String newContents){
        if(contents.toString().equals("")) contents = new StringBuffer(newContents);
        else contents.append(newContents);
    }
    
    public String getName(){ return name; }
    
    public String getContents(){ return contents.toString(); }
    
    public String getSection(){
        return "<b>" + name +":</b><br>\n" + getContents();
    }
    
    public boolean equals(Object other){
        if(other instanceof PageSection){
            if(((PageSection)other).getName().equals(name)){
                return true;
            }
        }
        return false;
    }
    
    public int hashCode(){ return name.hashCode(); }
    
    public String toString(){ return name; }
    
    private String name;
    
    private StringBuffer contents = new StringBuffer("");
}

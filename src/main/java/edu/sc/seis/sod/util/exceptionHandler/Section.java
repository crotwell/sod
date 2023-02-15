/**
 * Section.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.util.exceptionHandler;

public class Section{
    public Section(String name, String contents){
        setName(name);
        setContents(contents);
    }
    
    public String getContents(){ return contents; }
    
    public void setContents(String contents) {
        this.contents = contents;
    }
    
    public String getName(){ return name; }
    
    public void setName(String name) {
        this.name = name;
    }
    
    private String name, contents;
}


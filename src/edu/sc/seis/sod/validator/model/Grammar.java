/**
 * Grammar.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Grammar {
    public Grammar(String filename){
        this.filename = filename;
    }

    public void include(Grammar grammar) {
        Iterator it = grammar.defs.values().iterator();
        while(it.hasNext()){ add((Definition)it.next()); }
        //TODO combine starts
    }

    public String getLoc() { return filename;}

    public Form getRoot(){ return start.getForm(); }

    public void addStart(Definition d){
        if(start == null){ start = d; }
        else{ start = start.combineWith(d); }
    }

    public void add(Definition d){
        //TODO handle combines
        defs.put(d.getName(), d);
    }

    public boolean equals(Object o){
        if(o == this){ return true; }
        else if(o instanceof Grammar){
            return ((Grammar)o).filename.equals(filename);
        }
        return false;
    }

    public int hashCode(){ return filename.hashCode(); }

    public Definition getDef(String name){
        if(name == null){ return start; }
        return (Definition)defs.get(name);
    }

    public String toString(){ return "Grammar " + filename; }

    private Definition start;
    private String filename;
    private Map defs = new HashMap();
}


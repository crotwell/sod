/**
 * Grammar.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Grammar {
    public Grammar(String filename){
        this.filename = filename;
    }

    public List getDefs() {
        List defs = new ArrayList();
        defs.addAll(this.defs.values());
        return defs;
    }

    public void include(Grammar grammar) {
        Iterator it = grammar.defs.values().iterator();
        while(it.hasNext()){ add((Definition)it.next()); }
        //TODO combine starts
    }

    public String getLoc() { return filename;}

    public Form getRoot(){ return getDef("").getForm(); }

    public void add(Definition d){
        defs.put(d.getName(), d);
    }

    public void add(String name, Definition d){
        //TODO handle combines
        defs.put(name, d);
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
        return (Definition)defs.get(name);
    }

    public String toString(){ return "Grammar " + filename; }

    private String filename;
    private Map defs = new HashMap();
}


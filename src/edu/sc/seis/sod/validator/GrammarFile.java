/**
 * RelaxFile.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;

public class GrammarFile extends Grammar{
    public GrammarFile(String filename){
        this.filename = filename;
    }

    public Pattern getParent() {
        throw new UnsupportedOperationException(this + " has no parent!");
    }

    public String toString(){ return "Grammar based on " + filename; }

    private String filename;
}


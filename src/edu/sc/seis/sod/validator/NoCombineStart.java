/**
 * NoCombineStart.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;

public class NoCombineStart extends Start{
    public NoCombineStart(Grammar parent){ super(parent); }

    public void addChildren(Pattern[] children) {
        // TODO
    }

    public void combineWith(Start otherStart) {
        throw new UnsupportedOperationException("Can't combine " + this + " with " + otherStart);
    }

    public String toString(){ return "No Combine start in " + getParent(); }
}


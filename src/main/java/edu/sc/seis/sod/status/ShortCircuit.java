package edu.sc.seis.sod.status;

/**
 * @author groves Created on Mar 11, 2005
 */
public class ShortCircuit extends StringTreeLeaf {

    private static final String SHORTCIRCUIT = "ShortCircuit";

    public ShortCircuit(Object actor) {
        super(actor, false, SHORTCIRCUIT);
    }

    public String toString() {
        return SHORTCIRCUIT;
    }
}
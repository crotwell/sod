/**
 * EmbeddedRelaxGrammar.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;

public class EmbeddedGrammar extends Grammar {
    public EmbeddedGrammar(Grammar parent){ this.parent = parent; }

    public Pattern getParent(){ return parent; }

    private Grammar parent;
}


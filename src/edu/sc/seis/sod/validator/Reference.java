/**
 * Reference.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;

public class Reference extends Definition{
    public Reference(Grammar owner, String name){ super(owner, name);}

    public void setDefinition(Definition referencedDefinition){
        def = referencedDefinition;
    }

    public Pattern[] getKids(){ return def.getKids(); }

    public String toString(){ return "reference to " + getName(); }

    private Definition def;
}


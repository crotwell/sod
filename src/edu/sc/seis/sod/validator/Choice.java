/**
 * Choice.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;

public class Choice extends AbstractPattern{
    public Choice(Grammar owner, Pattern parent){
        super(owner, parent);
    }

    public String toString(){ return "element choice"; }
}


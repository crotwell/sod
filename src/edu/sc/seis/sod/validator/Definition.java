/**
 * Definition.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;

import java.util.ArrayList;
import java.util.List;

public class Definition extends AbstractPattern implements Pattern{
    public Definition(Grammar owner, String name){
        super(owner, null);
        this.name = name;
    }

    public String getName() { return name; }

    public String toString(){ return "Definition of " + name; }

    private String name;
}

/**
 * Text.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.validator.model.datatype.AnyText;

public class Text extends Empty{
    public Text(){ this(null); }

    public Text(Form parent){ super(parent); }

    public FormProvider  copyWithNewParent(Form newParent){
        return new Text(newParent);
    }

    public boolean equals(Object o){
        if(o == this){ return true; }
        if( o instanceof Text){ return super.equals(o); }
        return false;
    }

    public String toString(){ return "Any Text"; }

    public void accept(FormVisitor v) { v.visit(this);}
}

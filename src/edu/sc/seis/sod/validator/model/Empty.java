/**
 * Empty.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public class Empty extends AbstractForm{
    public Empty(){ this(null); }

    public Empty(Form parent){
        super(1, 1, parent);
    }

    public FormProvider  copyWithNewParent(Form newParent) {
        return new Empty(newParent);
    }

    public void accept(FormVisitor v) { v.visit(this);}
}

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
        Empty e = new Empty(newParent);
        e.setAnnotation(getAnnotation());
        return e;
    }

    public void accept(FormVisitor v) { v.visit(this);}
}

/**
 * NotAllowed.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public class NotAllowed extends AbstractForm{
    public NotAllowed(){ this(null); }

    public NotAllowed(Form parent){ super(1, 1, parent); }
    public void accept(FormVisitor v) {
        v.visit(this);
    }

    public FormProvider copyWithNewParent(Form newParent) {
        return new NotAllowed(newParent);
    }
}


/**
 * Interleave.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public class Interleave extends AbstractMultigenitorForm {
    public Interleave(int min, int max){ this(min, max, null); }

    public Interleave(int min, int max, Form parent){ super(min, max, parent); }

    public FormProvider copyWithNewParent(Form newParent) {
        Interleave i = new Interleave(getMin(), getMax(), newParent);
        copyKidsToNewParent(i);
        i.setAnnotation(getAnnotation());
        return i;
    }

    public void accept(FormVisitor v) {
        v.visit(this);
        super.accept(v);
        v.leave(this);
    }
}


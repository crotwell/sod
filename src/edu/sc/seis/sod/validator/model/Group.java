/**
 * Group.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public class Group extends AbstractMultigenitorForm {
    public Group(int min, int max){ super(min, max); }

    public Group(int min, int max, Form parent){ super(min, max, parent); }

    public FormProvider copyWithNewParent(Form newParent) {
        Group g = new Group(getMin(), getMax(), newParent);
        copyKidsToNewParent(g);
        g.setAnnotation(getAnnotation());
        return g;
    }

    public void accept(FormVisitor v) {
        v.visit(this);
        super.accept(v);
        v.leave(this);
    }
}


/**
 * Empty.java
 * 
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.validator.tour.Tourist;

public class Empty extends AbstractForm {
    public Empty() {
        this(null);
    }

    public Empty(Form parent) {
        super(1, 1, parent);
    }

    public FormProvider copyWithNewParent(Form newParent) {
        Empty e = new Empty(newParent);
        super.copyGutsOver(e);
        return e;
    }

    public void accept(Tourist v) {
        v.visit(this);
    }
}
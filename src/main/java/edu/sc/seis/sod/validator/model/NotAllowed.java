/**
 * NotAllowed.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.validator.tour.Tourist;

public class NotAllowed extends AbstractForm{
    public NotAllowed(){ this(null); }

    public NotAllowed(Form parent){ super(1, 1, parent); }
    public void accept(Tourist v) {
        v.visit(this);
    }

    public FormProvider copyWithNewParent(Form newParent) {
        NotAllowed na = new NotAllowed(newParent);
        super.copyGutsOver(na);
        return na;
    }
}


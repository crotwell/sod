/**
 * Choice.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public class Choice
    extends AbstractMultigenitorForm {
    public Choice(int min, int max){ this(min, max, null); }

    public Choice(int min, int max, Form parent){
        super(min, max, parent);
    }

    public FormProvider copyWithNewParent(Form newParent) {
        Choice c = new Choice(getMin(), getMax(), newParent);
        copyKidsToNewParent(c);
        return c;
    }

    public boolean equals(Object o){
        if(o == this){ return true; }
        if(o instanceof Choice){ return super.equals(o); }
        return false;
    }

    public void accept(FormVisitor v) {
        v.visit(this);
        super.accept(v);
        v.leave(this);
    }
}


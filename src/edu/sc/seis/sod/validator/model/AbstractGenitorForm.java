/**
 * AbstractGenitorForm.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.validator.ModelWalker;
import edu.sc.seis.sod.validator.tour.Tourist;

public abstract class AbstractGenitorForm extends AbstractForm implements GenitorForm{
    public AbstractGenitorForm(int min, int max){
        super(min, max);
    }

    public AbstractGenitorForm(int min, int max, Form parent){
        super(min, max, parent);
    }

    void setChild(FormProvider child){
        this.child = child.copyWithNewParent(this);
    }

    public boolean isAncestorOf(Form f){
        if(getChild().equals(f)){ return true; }
        else if(!ModelWalker.isSelfReferential(this)) {
            return getChild().isAncestorOf(f);
        }
        return false;
    }

    public Form getChild() { return child.getForm(); }

    public void accept(Tourist visitor){
        if(!ModelWalker.isSelfReferential(this)){ getChild().accept(visitor);  }
    }

    private FormProvider child;

    public void copyGutsOver(AbstractGenitorForm newParent) {
        newParent.setChild(child.copyWithNewParent(newParent));
        super.copyGutsOver(newParent);
    }
}


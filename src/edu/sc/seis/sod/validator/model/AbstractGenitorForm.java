/**
 * AbstractGenitorForm.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.validator.ModelWalker;

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

    void copyChildToNewParent(AbstractGenitorForm newParent){
        newParent.setChild(child.copyWithNewParent(newParent));
    }

    public void accept(FormVisitor visitor){
        if(!ModelWalker.isSelfReferential(this)){ getChild().accept(visitor);  }
    }

    private FormProvider child;
}


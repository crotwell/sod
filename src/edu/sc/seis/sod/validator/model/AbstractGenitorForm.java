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

    void setChild(FormProvider child){ this.child = child; }

    public boolean isAncestorOf(Form f){
        if(getChild().equals(f)){ return true; }
        else { return getChild().isAncestorOf(f); }
    }

    public Form getChild() { return child.getForm(); }

    public boolean equals(Object o){
        if(o == this){ return true; }
        if(o instanceof AbstractGenitorForm){
            return ((AbstractGenitorForm)o).getChild().equals(child) &&
                super.equals(o);
        }
        return false;
    }

    void copyChildToNewParent(AbstractGenitorForm newParent){
        newParent.setChild(child.copyWithNewParent(newParent));
    }

    public int hashCode(){
        return super.hashCode() * 37 + getChild().hashCode() * 37;
    }

    public void accept(FormVisitor visitor){
        if(!ModelWalker.isSelfReferential(this)){ getChild().accept(visitor);  }
    }

    private FormProvider child;
}


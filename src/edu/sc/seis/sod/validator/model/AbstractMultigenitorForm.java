/**
 * AbstractMultigenetorForm.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.validator.ModelWalker;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMultigenitorForm extends AbstractForm implements MultigenitorForm{
    public AbstractMultigenitorForm(int min, int max){ super(min, max); }

    public AbstractMultigenitorForm(int min, int max, Form parent){
        super(min, max, parent);
    }

    public Form[] getChildren() {
        for (int i = 0; i < kids.size(); i++) {
            if(kids.get(i) instanceof Ref){
                Ref kid = (Ref)kids.remove(i);
                kids.add(i, kid.getForm());

            }
        }
        return (Form[])kids.toArray(new Form[kids.size()]);
    }

    public NamedElement[] getElementalChildren() {
        // TODO
        return null;
    }

    void add(FormProvider newChild){
        if(newChild == this){ System.out.println("ADDING SELF");
            try{
                throw new RuntimeException();
            }catch(RuntimeException e){ e.printStackTrace(); }
        }
        kids.add(newChild);
    }

    public void accept(FormVisitor v) {
        if(!ModelWalker.isSelfReferential(this)){
            Form[] children = getChildren();
            for (int i = 0; i < children.length; i++) { children[i].accept(v); }
        }
    }

    void copyKidsToNewParent(AbstractMultigenitorForm newParent){
        for (int i = 0; i < kids.size(); i++) {
            if(kids.get(i) instanceof Ref){
                Ref kid = (Ref)kids.get(i);
                newParent.add(kid.copyWithNewParent(newParent));
            }else{
                Form kid = (Form)kids.get(i);
                newParent.add(kid.copyWithNewParent(newParent));
            }
        }
    }

    int getNumChildren(){ return kids.size(); }

    FormProvider[] getFormProviders(){
        return (FormProvider[])kids.toArray(new FormProvider[kids.size()]);
    }

    private List kids = new ArrayList();

}


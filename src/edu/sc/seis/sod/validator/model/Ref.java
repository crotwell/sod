/**
 * Ref.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public class Ref implements FormProvider{
    public Ref(Grammar owner){ this(owner, ""); }

    public Ref(Grammar owner, String name){ this(owner, name, null); }

    public Ref(Grammar owner, String name, Form parent){
        this.owner = owner;
        this.name = name;
        this.parent = parent;
        ann.setFormProvider(this);
    }

    public FormProvider copyWithNewParent(Form newParent) {
        Ref copy = new Ref(owner, name, newParent);
        copy.setMin(min);
        copy.setMax(max);
        if(ann != null){ copy.setAnnotation(ann); }
        return copy;
    }

    public Form getForm(){
        if(derefedForm == null){
            Definition def = getDef();
            Form refedForm = def.getForm();
            derefedForm = refedForm.deref(parent, def);
            if(getMin() == 0) { derefedForm.setMin(getMin()); }
            if(getMax() > 1) { derefedForm.setMax(getMax()); }
            derefedForm.setAnnotation(ann);
        }
        return derefedForm;
    }

    public String getName(){ return name; }

    public Definition getDef(){ return owner.getDef(name); }

    public int getMin() { return min; }

    public void setMin(int min) { this.min = min; }

    public int getMax() { return max; }

    public void setAnnotation(Annotation ann) {
        this.ann = ann;
        ann.setFormProvider(this);
    }

    public void setMax(int max) { this.max = max; }

    public int hashCode(){
        int result = 382;
        result += 37 * owner.hashCode();
        result += 37 * name.hashCode();
        return result + 37 * parent.hashCode();
    }

    private int max = 1, min = 1;
    private Grammar owner;
    private String name = "";
    private Form parent;
    private Form derefedForm;
    private Annotation ann = new Annotation();
}

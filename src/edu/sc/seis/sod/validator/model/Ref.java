/**
 * Ref.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public class Ref implements FormProvider{
    public Ref(Grammar owner){ this.owner = owner; }

    public Ref(Grammar owner, String name){ this(owner, name, null); }

    public Ref(Grammar owner, String name, Form parent){
        this.owner = owner;
        this.name = name;
        this.parent = parent;
    }

    public FormProvider copyWithNewParent(Form newParent) {
        return new Ref(owner, name, newParent);
    }

    public Form getForm(){
        Definition def = owner.getDef(name);
        Form refedForm = def.getForm();
        Form derefedForm = refedForm.deref(parent, def);
        derefedForm.setMin(getMin());
        derefedForm.setMax(getMax());
        return derefedForm;
    }

    public boolean equals(Object o){
        if(o == this){ return true; }
        if(o instanceof Ref){
            Ref oRef = (Ref)o;
            return oRef.owner.equals(owner) && oRef.name.equals(name) &&
                oRef.parent.equals(parent);
        }
        return false;
    }

    public int getMin() { return min; }

    public void setMin(int min) { this.min = min; }

    public int getMax() { return max; }

    public void setMax(int max) { this.max = max; }

    public int hashCode(){
        int result = 382;
        result += 37 * owner.hashCode();
        result += 37 * name.hashCode();
        return result + 37 * parent.hashCode();
    }

    private int max = 1, min = 1;
    private Grammar owner;
    private String name;
    private Form parent;
}

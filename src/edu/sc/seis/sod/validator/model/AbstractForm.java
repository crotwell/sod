/**
 * AbstractForm.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public abstract class AbstractForm implements Form {
    public AbstractForm(int min, int max){ this(min, max, null); }

    public AbstractForm(int min, int max, Form parent){
        this.min = min;
        this.max = max;
        this.parent = parent;
    }

    public boolean isAncestorOf(Form f) { return false; }

    public Form getParent() { return parent; }

    public int getMin() { return min; }

    public int getMax() { return max;}

    public void setMin(int min) { this.min = min; }

    public Form getForm() { return this; }

    public void setMax(int max) { this.max = max; }

    public boolean isFromDef() { return getDef() != null;}

    public Definition getDef() { return def; }

    public Form deref(Form newParent, Definition def){
        AbstractForm copy = (AbstractForm)copyWithNewParent(newParent);
        copy.def = def;
        return copy;
    }

    public boolean equals(Object o){
        if(o == this){ return true; }
        if(o instanceof AbstractForm){
            AbstractForm af = (AbstractForm)o;
            if(af.min == min && af.max == max){
                if(af.parent != null && af.parent.equals(parent)){ return true; }
                return af.parent == null && parent == null;
            }
        }
        return false;
    }

    public void setAnnotation(Annotation ann){ this.ann = ann; }

    public Annotation getAnnotation(){ return ann; }

    private Definition def;
    private int min, max;
    private Form parent;
    private Annotation ann;
}


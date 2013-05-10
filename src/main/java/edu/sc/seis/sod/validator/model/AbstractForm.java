/**
 * AbstractForm.java
 * 
 * @author Charles Groves
 */
package edu.sc.seis.sod.validator.model;

public abstract class AbstractForm implements Form {

    public AbstractForm(int min, int max) {
        this(min, max, null);
    }

    public AbstractForm(int min, int max, Form parent) {
        this.min = min;
        this.max = max;
        this.parent = parent;
        ann.setFormProvider(this);
    }
    
    public boolean isAncestorOf(Form f){
        return isAncestorOf(f, null);
    }

    public boolean isAncestorOf(Form f, Form root) {
        return false;
    }

    public Form getParent() {
        return parent;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public Form getForm() {
        return this;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public boolean isFromDef() {
        return getDef() != null;
    }

    public Definition getDef() {
        return def;
    }

    public Form deref(Form newParent, Definition newDef) {
        AbstractForm copy = (AbstractForm)copyWithNewParent(newParent);
        copy.def = newDef;
        return copy;
    }

    public void setAnnotation(Annotation ann) {
        this.ann = ann;
        ann.setFormProvider(this);
    }

    public Annotation getAnnotation() {
        return ann;
    }

    public void setNamespace(String ns) {
        namespace = ns;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getXPath() {
        if (parent == null) { return "/";}
        return getParent().getXPath();
    }

    public void copyGutsOver(AbstractForm copy) {
        copy.setAnnotation(getAnnotation().makeCopyWithNewFormProvider(copy));
        copy.def = getDef();
    }

    private Definition def;

    private int min, max;

    private Form parent;

    private Annotation ann = new Annotation();

    private String namespace;
}
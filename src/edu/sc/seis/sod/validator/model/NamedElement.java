/**
 * NamedElement.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public class NamedElement extends AbstractGenitorForm{
    public NamedElement(int min, int max, String name){
        this(min, max, name, null);
    }

    public NamedElement(int min, int max, String name, Form parent){
        super(min, max, parent);
        this.name = name;
    }

    public Attribute[] getAttributes(){
        //TODO
        return null;
    }

    public NamedElement[] getElementalChildren(){
        //TODO
        return null;
    }

    public String getName(){ return name; }

    public FormProvider copyWithNewParent(Form newParent) {
        NamedElement copy = new NamedElement(getMin(), getMax(), getName(), newParent);
        copyChildToNewParent(copy);
        return copy;
    }

    public boolean equals(Object o){
        if(this == o){ return true; }
        if(o instanceof NamedElement){
            return ((NamedElement)o).getName().equals(name) && super.equals(o);
        }
        return false;
    }

    public int hashCode(){
        return super.hashCode() * 37 + getName().hashCode() * 37;
    }

    public void accept(FormVisitor v){
        v.visit(this);
        super.accept(v);
        v.leave(this);
    }

    private String name;
}

/**
 * Attribute.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public class Attribute extends AbstractGenitorForm{
    public Attribute(int min, int max, String name){
        super(min, max);
        this.name = name;
    }
    public Attribute(int min, int max, String name, Form parent){
        super(min, max, parent);
        this.name = name;
    }

    public FormProvider copyWithNewParent(Form newParent) {
        Attribute attr = new Attribute(getMin(), getMax(), getName(), newParent);
        copyChildToNewParent(attr);
        return attr;
    }

    public String getName(){ return name; }

    public boolean equals(Object o){
        if(o == this){ return true; }
        if(o instanceof Attribute){
            return ((Attribute)o).getName().equals(getName()) &&
                super.equals(o);
        }
        return false;
    }

    public int hashCode(){
        return super.hashCode() * 37 + getName().hashCode() * 37;
    }

    public void accept(FormVisitor v) {
        v.visit(this);
        super.accept(v);
        v.leave(this);
    }

    private String name;
}


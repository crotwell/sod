/**
 * Attribute.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.validator.tour.Tourist;

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
        super.copyGutsOver(attr);
        return attr;
    }

    public String getName(){ return name; }

    public void accept(Tourist v) {
        v.visit(this);
        super.accept(v);
        v.leave(this);
    }

    public String toString(){
        return "@"+name;
    }

    private String name;
}


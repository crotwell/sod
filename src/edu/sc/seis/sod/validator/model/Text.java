/**
 * Text.java
 * 
 * @author Charles Groves
 */
package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.validator.tour.Tourist;

public class Text extends AbstractForm{

    public Text() {
        this(null);
    }

    public Text(Form parent) {
        super(1, 1, parent);
    }

    public FormProvider copyWithNewParent(Form newParent) {
        Text t = new Text(newParent);
        super.copyGutsOver(t);
        return t;
    }

    public String toString() {
        return "Any Text";
    }

    public void accept(Tourist v) {
        v.visit(this);
    }
}
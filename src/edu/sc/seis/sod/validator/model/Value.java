/**
 * Value.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.validator.model.datatype.Token;
import edu.sc.seis.sod.validator.tour.Tourist;

public class Value extends Data{
    /**
     * creates a Value object with the default Token datatype
     */
    public Value(int min, int max, String value){
        this(min, max, value, new Token());
    }

    public Value(int min, int max, String value, ModelDatatype datatype){
        super(min, max, datatype);
        this.value = value;
    }

    public Value(int min, int max, String value, ModelDatatype datatype, Form parent){
        super(min, max, datatype, parent);
        this.value = value;
    }

    public String toString(){
        return "Value: " + getValue();
    }

    public String getValue(){ return value; }

    public FormProvider copyWithNewParent(Form newParent){
        Value v = new Value(getMin(), getMax(), value, getDatatype(), newParent);
        super.copyGutsOver(v);
        return v;
    }

    public void accept(Tourist v) { v.visit(this);}

    private String value;
}

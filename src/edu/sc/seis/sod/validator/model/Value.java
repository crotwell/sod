/**
 * Value.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.validator.model.datatype.Token;

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
        return new Value(getMin(), getMax(), value, getDatatype(), newParent);
    }

    public void accept(FormVisitor v) { v.visit(this);}

    private String value;
}

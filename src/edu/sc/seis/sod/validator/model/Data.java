/**
 * Data.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public class Data extends AbstractForm{
    public Data(int min, int max, ModelDatatype datatype){
        this(min, max, datatype, null);
    }

    public Data(int min, int max, ModelDatatype datatype, Form parent){
        super(min, max, parent);
        this.datatype = datatype;
    }

    public FormProvider  copyWithNewParent(Form newParent) {
        Data d = new Data(getMin(), getMax(), getDatatype(), newParent);
        d.setAnnotation(getAnnotation());
        return d;
    }

    public int hashCode(){ return super.hashCode() * 37 + datatype.hashCode(); }

    public ModelDatatype getDatatype(){ return datatype; }

    public String toString(){ return "Data of type " + getDatatype(); }

    public void accept(FormVisitor v) { v.visit(this);}

    private ModelDatatype datatype;
}


package edu.sc.seis.sod.validator.model;



public class AnyXMLElement extends AbstractGenitorForm implements FormProvider {


    public AnyXMLElement(int min, int max) {
        super(min, max, null);
    }

    public AnyXMLElement(int min, int max, Form parent) {
        super(min, max, parent);
    }

    public FormProvider copyWithNewParent(Form newParent) {
        AnyXMLElement copy = new AnyXMLElement(getMin(), getMax(), newParent);
        super.copyGutsOver(copy);
        return copy;
    }
}

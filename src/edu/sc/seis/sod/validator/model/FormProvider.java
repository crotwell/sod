/**
 * FormProvider.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public interface FormProvider{
    public Form getForm();

    /**
     * @return   the minimum number of times this Form can appear
     */
    public int getMin();

    public void setMin(int min);

    /**
     * @return   the maximum number of times this Form can appear
     */
    public int getMax();

    public void setMax(int max);

    public FormProvider copyWithNewParent(Form newParent);

    public void setAnnotation(Annotation ann);
}


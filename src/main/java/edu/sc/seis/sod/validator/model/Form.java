/**
 * Form.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.validator.tour.Tourist;

public interface Form extends FormProvider{
    public String getXPath();

    public Annotation getAnnotation();

    /**
     * @return   the Form containing this Form.  Every Form has a parent except
     * for the root which returns null
     */
    public Form getParent();

    /**
     * @return   true if this From is originally from a Definition.  If true,
     * getDef() will return this Form's definition
     */
    public boolean isFromDef();


    /**
     * @return   the definition this form was originally contained in.  Null if
     * this form was not contained in a definition
     */
    public Definition getDef();

    /**
     * @return	the namespace of this form
     */
    public String getNamespace();
    
    /**
     * @return  true if the passed in Form is directly descended from this Form
     */
    public boolean isAncestorOf(Form f);
    public boolean isAncestorOf(Form f, Form root);

    /**
     * @return   this form set with parent as its parent, isFromDef returns true
     * and getDef returns def
     *
     */
    public Form deref(Form parent, Definition def);
    
    public void accept(Tourist v);
}

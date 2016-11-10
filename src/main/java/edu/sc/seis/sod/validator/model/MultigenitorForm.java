/**
 * MultigenitorStructure.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public interface MultigenitorForm extends Form{
    /**
     * @return all of the Forms that go inside of this form.
     */
    public Form[] getChildren();

    public NamedElement[] getElementalChildren();

    public Attribute[] getAttributes();
}


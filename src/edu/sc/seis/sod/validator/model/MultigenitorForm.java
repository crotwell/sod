/**
 * MultigenitorStructure.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public interface MultigenitorForm extends Form{
    /**
     * @returns all of the Forms that go inside of this form.
     */
    public Form[] getChildren();

    public NamedElement[] getElementalChildren();
}


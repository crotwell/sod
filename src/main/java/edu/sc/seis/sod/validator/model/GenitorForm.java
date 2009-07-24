/**
 * GenitorStructure.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public interface GenitorForm extends Form {
    /**
     * @return the Form that goes inside this one
     */
    public Form getChild();
}

/**
 * Datatype.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import org.relaxng.datatype.Datatype;

public interface ModelDatatype extends Datatype{
    public String getDescription();
    
    public String getExampleValue();
}


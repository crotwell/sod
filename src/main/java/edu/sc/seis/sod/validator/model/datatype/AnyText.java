/**
 * Text.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model.datatype;


import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeStreamingValidator;
import org.relaxng.datatype.ValidationContext;

import edu.sc.seis.sod.validator.model.ModelDatatype;

public class AnyText implements ModelDatatype{
    public boolean isValid(String p1, ValidationContext p2) {
        // TODO
        return false;
    }

    public void checkValid(String p1, ValidationContext p2) throws DatatypeException {
        // TODO
    }

    public DatatypeStreamingValidator createStreamingValidator(ValidationContext p1) {
        // TODO
        return null;
    }

    public Object createValue(String p1, ValidationContext p2) {
        // TODO
        return null;
    }

    public boolean sameValue(Object p1, Object p2) {
        // TODO
        return false;
    }

    public int valueHashCode(Object p1) {
        // TODO
        return 0;
    }

    public int getIdType() {
        // TODO
        return 0;
    }

    public boolean isContextDependent() {
        // TODO
        return false;
    }

    public String getDescription(){ return "Any text"; }

    public String toString(){ return getDescription(); }

    public boolean equals(Object o){
        //TODO
        return o == this;
    }

    public int hashCode(){
        //TODO
        return super.hashCode();
    }
    
    public String getExampleValue(){
        return "text";
    }

}


/**
 * DoubleDatatype.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model.datatype;

import edu.sc.seis.sod.validator.model.ModelDatatype;
import org.relaxng.datatype.DatatypeStreamingValidator;
import org.relaxng.datatype.ValidationContext;
import org.relaxng.datatype.DatatypeException;

public class DoubleDatatype implements ModelDatatype {

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

    public String getDescription() { return "double"; }

    public String toString(){ return getDescription(); }
    
    public String getExampleValue(){
        return "12.57";
    }

}


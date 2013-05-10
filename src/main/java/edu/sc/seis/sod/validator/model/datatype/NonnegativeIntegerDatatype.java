package edu.sc.seis.sod.validator.model.datatype;

import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeStreamingValidator;
import org.relaxng.datatype.ValidationContext;

import edu.sc.seis.sod.validator.model.ModelDatatype;

/**
 * @author groves Created on Jul 30, 2004
 */
public class NonnegativeIntegerDatatype implements ModelDatatype {

    public String getDescription() {
        return "A non negative integer";
    }

    public String getExampleValue() {
        return "12";
    }

    public String toString() {
        return "Non negative integer";
    }

    public boolean isValid(String arg0, ValidationContext arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    public void checkValid(String arg0, ValidationContext arg1)
            throws DatatypeException {
    // TODO Auto-generated method stub
    }

    public DatatypeStreamingValidator createStreamingValidator(ValidationContext arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object createValue(String arg0, ValidationContext arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean sameValue(Object arg0, Object arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    public int valueHashCode(Object arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getIdType() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isContextDependent() {
        // TODO Auto-generated method stub
        return false;
    }
}
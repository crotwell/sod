/*
 * Created on Jul 16, 2004
 */
package edu.sc.seis.sod.validator.model.datatype;

import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeStreamingValidator;
import org.relaxng.datatype.ValidationContext;
import edu.sc.seis.sod.validator.model.ModelDatatype;

/**
 * @author Charlie Groves
 */
public class IntegerDatatype implements ModelDatatype {

    public String getDescription() {
        return "integer";
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

    public String getExampleValue() {
        return "12";
    }
}
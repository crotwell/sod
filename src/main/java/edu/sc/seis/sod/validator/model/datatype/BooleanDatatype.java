package edu.sc.seis.sod.validator.model.datatype;

import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeStreamingValidator;
import org.relaxng.datatype.ValidationContext;

import edu.sc.seis.sod.validator.model.ModelDatatype;


public class BooleanDatatype  implements ModelDatatype {

    @Override
    public String getDescription() {
        
        return "True or False";
    }

    @Override
    public String getExampleValue() {
        return "TRUE";
    }
    
    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    public void checkValid(String value, ValidationContext arg1) throws DatatypeException {
        //not used
    }

    @Override
    public DatatypeStreamingValidator createStreamingValidator(ValidationContext arg0) {
        //not used
        return null;
    }

    @Override
    public Object createValue(String arg0, ValidationContext arg1) {
        //not used
        return null;
    }

    @Override
    public int getIdType() {
        //not used
        return 0;
    }

    @Override
    public boolean isContextDependent() {
        //not used
        return false;
    }

    @Override
    public boolean isValid(String arg0, ValidationContext arg1) {
        //not used
        return false;
    }

    @Override
    public boolean sameValue(Object arg0, Object arg1) {
        //not used
        return false;
    }

    @Override
    public int valueHashCode(Object arg0) {
        //not used
        return 0;
    }}

/**
 * VelocityModelHelper.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;

import edu.sc.seis.sod.validator.model.Choice;
import edu.sc.seis.sod.validator.model.Form;
import edu.sc.seis.sod.validator.model.Group;
import edu.sc.seis.sod.validator.model.Interleave;
import edu.sc.seis.sod.validator.model.MultigenitorForm;

public class VelocityModelHelper {
    public String getClass(Form f){
        String classAttr = "class=\"";
        if(f.getMin() == 0){
            if(f.getMax() == 1){ classAttr += "zeroToOne"; }
            else{ classAttr += "zeroToMany"; }
        }else{
            if(f.getMax() == 1){
                if(f instanceof MultigenitorForm){ classAttr += "one"; }
            }else{ classAttr += "oneToMany"; }
        }
        if(f instanceof MultigenitorForm){
            classAttr = appendMultigenitorClass((MultigenitorForm)f, classAttr);
        }
        return classAttr+"\"";
    }

    private String appendMultigenitorClass(MultigenitorForm mf, String classAttr) {
        if(mf instanceof Choice){ classAttr += "Choice"; }
        else if(mf instanceof Interleave){ classAttr += "Interleave"; }
        return classAttr;
    }

    public boolean isMultigen(Form f){ return f instanceof MultigenitorForm; }
}

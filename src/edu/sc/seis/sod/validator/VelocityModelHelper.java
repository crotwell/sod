/**
 * VelocityModelHelper.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;

import edu.sc.seis.sod.validator.model.*;

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

    public boolean isGen(Form f){ return f instanceof GenitorForm; }

    public boolean isChoice(Form f){ return f instanceof Choice; }

    public boolean isInterleave(Form f){ return f instanceof Interleave; }

    public boolean isGroup(Form f){ return f instanceof Group; }

    public int getLen(Object[] array){ return array.length; }

    public Object getItem(Object[] array, Integer index){ return array[index.intValue()]; }

    public String getDefName(Object o){
        if(o instanceof Definition){ return ((Definition)o).getName(); }
        if(o == null ){ return "NULL"; }
        //else if(!(o instanceof Form)){ retu
        //else return o.toString();
        return null;
    }
}

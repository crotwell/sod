/**
 * VelocityModelHelper.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.documenter;


import java.io.File;

import edu.sc.seis.sod.validator.model.Choice;
import edu.sc.seis.sod.validator.model.Definition;
import edu.sc.seis.sod.validator.model.Form;
import edu.sc.seis.sod.validator.model.GenitorForm;
import edu.sc.seis.sod.validator.model.Group;
import edu.sc.seis.sod.validator.model.Interleave;
import edu.sc.seis.sod.validator.model.MultigenitorForm;
import edu.sc.seis.sod.validator.model.NamedElement;

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

    public String getName(Object o){
        if(o instanceof Definition){ return getDefName((Definition)o); }
        else return o.toString();
    }

    public String getDefName(Object o){
        if(o instanceof Definition){
            return getDefName((Definition)o);
        }
        if(o == null ){ return "NULL"; }
        else if(o instanceof Form){
            return getDefName(SchemaDocumenter.getNearestDef((Form)o));
        }
        else return o.toString();
    }

    public String getDefName(Definition d){
        if(d.getForm() instanceof NamedElement){
            return ((NamedElement)d.getForm()).getName();
        }
        if(d.getName().equals("")){
            String filename = new File(d.getGrammar().getLoc()).getName();
            return filename.substring(0, filename.length() - 4) + " Start";
        }
        return d.getName();
    }

    public int length(Object[] objs){ return objs.length; }
}

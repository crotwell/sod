/**
 * ModelWalker.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;
import edu.sc.seis.sod.validator.model.Definition;
import edu.sc.seis.sod.validator.model.Form;

public class ModelWalker {
    public static boolean isSelfReferential(Form f){
        if(f.isFromDef()){  return lineageContainsRefTo(f, f.getDef()); }
        return false;
    }

    public static boolean lineageContainsRefTo(Form f, Definition def){
        Form parent = f.getParent();
        if(parent != null){
            if(def.equals(parent.getDef())){ return true; }
            return lineageContainsRefTo(parent, def);
        }
        return false;
    }
}

/**
 * ModelWalker.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;
import edu.sc.seis.sod.validator.model.Definition;
import edu.sc.seis.sod.validator.model.Form;
import edu.sc.seis.sod.validator.model.MultigenitorForm;
import edu.sc.seis.sod.validator.model.NamedElement;

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

    public static NamedElement getDescendantTowards(NamedElement parent,
                                                    NamedElement result){
        Form child = parent.getChild();
        if(child instanceof NamedElement && isTowards(child, result)){
            return (NamedElement)child;
        }else if(child instanceof MultigenitorForm){
            return getDescendantTowards((MultigenitorForm)child, result);
        }
        return null;
    }

    private static NamedElement getDescendantTowards(MultigenitorForm f,
                                                     NamedElement result){
        Form[] kids = f.getChildren();
        for (int i = 0; i < kids.length; i++) {
            if(kids[i] instanceof NamedElement && isTowards(kids[i], result)){
                return (NamedElement)kids[i];
            }else if(kids[i] instanceof MultigenitorForm){
                NamedElement subresult = getDescendantTowards((MultigenitorForm)kids[i],
                                                              result);
                if(subresult != null){ return subresult; }
            }
        }
        return null;
    }

    public static boolean isTowards(Form parent, Form result){
        return parent.isAncestorOf(result) || parent.equals(result);
    }

    public static NamedElement[] getSiblings(NamedElement brother){
        Form parent = brother.getParent();
        if(parent == null){ return new NamedElement[]{brother}; }
        while(! (parent instanceof NamedElement)){ parent = parent.getParent();}
        return ((NamedElement)parent).getElementalChildren();
    }
}

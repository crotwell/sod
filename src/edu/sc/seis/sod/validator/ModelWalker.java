/**
 * ModelWalker.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;
import edu.sc.seis.sod.validator.model.*;

import java.util.ArrayList;
import java.util.List;

public class ModelWalker {
    public static Form[] getAllInstances(Form root, Definition def){
        List instances = new ArrayList();
        getAllInstances(root, def, instances);
        return (Form[])instances.toArray(new Form[instances.size()]);
    }

    public static void getAllInstances(Form root, Definition def, List accumInstance){
        if(!isSelfReferential(root)){
            if(def.equals(root.getDef())){ accumInstance.add(root); }
            if(root instanceof GenitorForm){
                getAllInstances(((GenitorForm)root).getChild(), def, accumInstance);
            }else if(root instanceof MultigenitorForm){
                MultigenitorForm multiRoot = (MultigenitorForm)root;
                Form[] kids = multiRoot.getChildren();
                for (int i = 0; i < kids.length; i++) {
                    getAllInstances(kids[i], def, accumInstance);
                }
            }
        }
    }

    public static boolean isSelfReferential(Form f){
        if(f.isFromDef()){  return lineageContainsRefTo(f, f.getDef()); }
        return false;
    }

    public static boolean requiresSelfReferentiality(Form f){
        if (f.getMin() == 0) return false;
        if (isSelfReferential(f)) return true;
        if (f instanceof NamedElement){
            NamedElement el = (NamedElement)f;
            Form kid = el.getChild();
            return requiresSelfReferentiality(kid);
        } else if (f instanceof Choice){
            Choice c = (Choice)f;
            Form[] kids = c.getChildren();
            for (int i = 0; i < kids.length; i++) {
                if (!requiresSelfReferentiality(kids[i])) return false;
            }
            return true;
        } else if (f instanceof Interleave || f instanceof Group){
            MultigenitorForm multi = (MultigenitorForm)f;
            Form[] kids = multi.getChildren();
            for (int i = 0; i < kids.length; i++) {
                if (requiresSelfReferentiality(kids[i])) return false;
            }
            return true;
        }
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

/**
 * ModelWalker.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;
import edu.sc.seis.sod.validator.model.*;

import edu.sc.seis.sod.validator.documenter.SchemaDocumenter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ModelWalker {
    public static Definition[] getContainingDefs(Form root, Definition def){
        Set instances = new HashSet();
        getContainingDefs(root, def, instances);
        return (Definition[])instances.toArray(new Definition[instances.size()]);
    }

    public static void getContainingDefs(Form root, Definition def, Collection accumDefs){
        if(def.equals(root.getDef())){
            if(root.getParent() == null){ return ; }
            accumDefs.add(SchemaDocumenter.getNearestDef(root.getParent()));
        }
        if(!isSelfReferential(root)){
            if(root instanceof GenitorForm){
                getContainingDefs(((GenitorForm)root).getChild(), def, accumDefs);
            }else if(root instanceof MultigenitorForm){
                MultigenitorForm multiRoot = (MultigenitorForm)root;
                Form[] kids = multiRoot.getChildren();
                for (int i = 0; i < kids.length; i++) {
                    getContainingDefs(kids[i], def, accumDefs);
                }
            }
        }
    }

    public static Form[] getAllInstances(Form root, Definition def){
        Set instances = new HashSet();
        getAllInstances(root, def, instances);
        return (Form[])instances.toArray(new Form[instances.size()]);
    }

    public static void getAllInstances(Form root, Definition def, Collection accumInstance){
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

    public static Form getInstance(Form root, Definition def){
        if(root.isFromDef() && root.getDef().equals(def)){ return root; }
        else if(!isSelfReferential(root)){
            if(root instanceof GenitorForm){
                return getInstance(((GenitorForm)root).getChild(), def);
            }else if(root instanceof MultigenitorForm){
                MultigenitorForm multiRoot = (MultigenitorForm)root;
                Form[] kids = multiRoot.getChildren();
                for (int i = 0; i < kids.length; i++) {
                    Form result = getInstance(kids[i], def);
                    if(result != null){ return result; }
                }

            }
        }
        return null;
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

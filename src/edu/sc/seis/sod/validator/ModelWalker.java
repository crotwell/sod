/**
 * ModelWalker.java
 * 
 * @author Charles Groves
 */
package edu.sc.seis.sod.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import edu.sc.seis.sod.validator.documenter.SchemaDocumenter;
import edu.sc.seis.sod.validator.model.Choice;
import edu.sc.seis.sod.validator.model.Definition;
import edu.sc.seis.sod.validator.model.Form;
import edu.sc.seis.sod.validator.model.GenitorForm;
import edu.sc.seis.sod.validator.model.Group;
import edu.sc.seis.sod.validator.model.Interleave;
import edu.sc.seis.sod.validator.model.MultigenitorForm;
import edu.sc.seis.sod.validator.model.NamedElement;

public class ModelWalker {

    public static Definition[] getContainingDefs(Form root, Definition def) {
        Set instances = new HashSet();
        getContainingDefs(root, def, instances);
        return (Definition[])instances.toArray(new Definition[instances.size()]);
    }

    public static void getContainingDefs(Form root,
                                         Definition def,
                                         Collection accumDefs) {
        if(def.equals(root.getDef())) {
            if(root.getParent() == null) { return; }
            accumDefs.add(SchemaDocumenter.getNearestDef(root.getParent()));
        }
        if(!isSelfReferential(root)) {
            if(root instanceof GenitorForm) {
                getContainingDefs(((GenitorForm)root).getChild(),
                                  def,
                                  accumDefs);
            } else if(root instanceof MultigenitorForm) {
                MultigenitorForm multiRoot = (MultigenitorForm)root;
                Form[] kids = multiRoot.getChildren();
                for(int i = 0; i < kids.length; i++) {
                    getContainingDefs(kids[i], def, accumDefs);
                }
            }
        }
    }

    public static Form[] getAllInstances(Form root, Definition def) {
        Set instances = new HashSet();
        getAllInstances(root, def, instances);
        return (Form[])instances.toArray(new Form[instances.size()]);
    }

    public static void getAllInstances(Form root,
                                       Definition def,
                                       Collection accumInstance) {
        if(!isSelfReferential(root)) {
            if(def.equals(root.getDef())) {
                accumInstance.add(root);
            }
            if(root instanceof GenitorForm) {
                getAllInstances(((GenitorForm)root).getChild(),
                                def,
                                accumInstance);
            } else if(root instanceof MultigenitorForm) {
                MultigenitorForm multiRoot = (MultigenitorForm)root;
                Form[] kids = multiRoot.getChildren();
                for(int i = 0; i < kids.length; i++) {
                    getAllInstances(kids[i], def, accumInstance);
                }
            }
        }
    }

    public static Form getInstance(Form root, Definition def) {
        if(root.isFromDef() && root.getDef().equals(def)) {
            return root;
        } else if(!isSelfReferential(root)) {
            if(root instanceof GenitorForm) {
                return getInstance(((GenitorForm)root).getChild(), def);
            } else if(root instanceof MultigenitorForm) {
                MultigenitorForm multiRoot = (MultigenitorForm)root;
                Form[] kids = multiRoot.getChildren();
                for(int i = 0; i < kids.length; i++) {
                    Form result = getInstance(kids[i], def);
                    if(result != null) { return result; }
                }
            }
        }
        return null;
    }
    
    public static boolean isSelfReferential(Form f){
        return isSelfReferential(f, null);
    }

    public static boolean isSelfReferential(Form f, Form root) {
        if(f.isFromDef() && !f.equals(root)) { return lineageContainsRefTo(f, f.getDef(), root); }
        return false;
    }

    public static boolean requiresSelfReferentiality(Form f) {
        if(f.getMin() == 0) return false;
        if(isSelfReferential(f)) return true;
        if(f instanceof NamedElement) {
            NamedElement el = (NamedElement)f;
            Form kid = el.getChild();
            return requiresSelfReferentiality(kid);
        } else if(f instanceof Choice) {
            Choice c = (Choice)f;
            Form[] kids = c.getChildren();
            for(int i = 0; i < kids.length; i++) {
                if(!requiresSelfReferentiality(kids[i])) return false;
            }
            return true;
        } else if(f instanceof Interleave || f instanceof Group) {
            MultigenitorForm multi = (MultigenitorForm)f;
            Form[] kids = multi.getChildren();
            for(int i = 0; i < kids.length; i++) {
                if(requiresSelfReferentiality(kids[i])) { return true; }
            }
            return false;
        }
        return false;
    }

    public static boolean lineageContainsRefTo(Form f, Definition def) {
        return lineageContainsRefTo(f, def, null);
    }

    public static boolean lineageContainsRefTo(Form f, Definition def, Form root) {
        Form parent = f.getParent();
        if(parent == null || def == null || f.equals(root)) { return false; }
        if(def.equals(parent.getDef())) { return true; }
        return lineageContainsRefTo(parent, def, root);
    }

    public static NamedElement getDescendantTowards(NamedElement parent,
                                                    NamedElement result) {
        Form child = parent.getChild();
        if(child instanceof NamedElement && isTowards(child, result)) {
            return (NamedElement)child;
        } else if(child instanceof MultigenitorForm) { return getDescendantTowards((MultigenitorForm)child,
                                                                                   result); }
        return null;
    }

    private static NamedElement getDescendantTowards(MultigenitorForm f,
                                                     NamedElement result) {
        Form[] kids = f.getChildren();
        for(int i = 0; i < kids.length; i++) {
            if(kids[i] instanceof NamedElement && isTowards(kids[i], result)) {
                return (NamedElement)kids[i];
            } else if(kids[i] instanceof MultigenitorForm) {
                NamedElement subresult = getDescendantTowards((MultigenitorForm)kids[i],
                                                              result);
                if(subresult != null) { return subresult; }
            }
        }
        return null;
    }

    public static boolean isTowards(Form parent, Form result) {
        //System.out.println("isTowards called");
        //System.out.println("PARENT=" + ModelUtil.toString(parent));
       // System.out.println("RESULT=" + ModelUtil.toString(result));
        boolean b = parent.isAncestorOf(result, parent) || parent.equals(result);
        //System.out.println("isTowards: " + b);
        return b;
    }

    public static int getDistance(Form base, Form result) {
        return getDistance(base, base, result);
    }

    private static int getDistance(Form initialBase, Form base, Form result) {
        if(result == null) { return -1; }
        if(result.equals(base)) { return 0; }
        if(base.isFromDef() && base != initialBase) {
            Form[] lineageToInitial = getLineage(base.getParent(), initialBase);
            for(int i = 0; i < lineageToInitial.length; i++) {
                Form cur = lineageToInitial[i];
                if(cur.isFromDef() && cur.getDef().equals(base.getDef())) { return -1; }
            }
        }
        if(base instanceof MultigenitorForm) {
            MultigenitorForm mgf = (MultigenitorForm)base;
            int minDist = Integer.MAX_VALUE;
            for(int i = 0; i < mgf.getChildren().length; i++) {
                Form cur = mgf.getChildren()[i];
                int curDist = getDistance(initialBase, cur, result);
                if(curDist < minDist && curDist > -1) {
                    minDist = curDist;
                }
            }
            if(minDist < Integer.MAX_VALUE) { return minDist; }
        } else if(base instanceof GenitorForm) {
            GenitorForm gf = (GenitorForm)base;
            int subDist = getDistance(initialBase, gf.getChild(), result);
            if(subDist > -1) { return subDist + 1; }
        }
        return -1;
    }

    public static NamedElement[] getSiblings(NamedElement brother) {
        Form parent = brother.getParent();
        if(parent == null) { return new NamedElement[] {brother}; }
        while(!(parent instanceof NamedElement)) {
            parent = parent.getParent();
        }
        return ((NamedElement)parent).getElementalChildren();
    }

    public static Form[] getLineage(Form f) {
        return getLineage(f, null);
    }

    public static Form[] getLineage(Form child, Form parent) {
        List lineageList = new ArrayList();
        Form temp = child;
        while(temp != parent) {
            lineageList.add(temp);
            temp = temp.getParent();
        }
        return (Form[])lineageList.toArray(new Form[0]);
    }

    public static boolean isInLineage(Form parent, Form result) {
        Form[] lineage = getLineage(result);
        for(int i = 0; i < lineage.length; i++) {
            if(lineage[i].equals(parent)) { return true; }
        }
        return false;
    }

    public static String getNamespaceFromAncestors(Form f) {
        String ns = null;
        Form temp = f;
        do {
            ns = temp.getNamespace();
            temp = temp.getParent();
        } while(temp != null && ns == null);
        return ns;
    }
}
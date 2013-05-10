/**
 * ModelUtil.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.validator.ModelWalker;

public class ModelUtil {

    public static String toString(Form f) {
        return toString(f, true);
    }

    public static String toString(Form[] f) {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < f.length; i++) {
            buf.append(toString(f[i], false) + ' ');
        }
        return buf.toString();
    }

    public static String toString(Form f, boolean continueFollowing) {
        if(f instanceof Attribute) {
            Attribute a = (Attribute)f;
            return "Attribute: " + a.getName();
        } else if(f instanceof MultigenitorForm) {
            StringBuffer buf = new StringBuffer();
            if(f instanceof Choice) {
                buf.append("Choice");
            } else if(f instanceof Interleave) {
                buf.append("Interleave");
            } else if(f instanceof Group) {
                buf.append("Group");
            } else {
                buf.append("Unknown MultigenitorForm");
            }
            MultigenitorForm c = (MultigenitorForm)f;
            Form[] kids = c.getChildren();
            if(continueFollowing) {
                buf.append(": [");
                buf.append(toString(kids));
                buf.append("]");
            }
            return buf.toString();
        } else if(f instanceof NamedElement) {
            NamedElement n = (NamedElement)f;
            return "NamedElement: " + n.getName();
        } else if(f instanceof Value) {
            return "Value";
        } else if(f instanceof Data) {
            return "Data";
        } else if(f instanceof NotAllowed) {
            return "NotAllowed";
        } else if(f instanceof Text) {
            return "Text";
        } else if(f instanceof Empty) {
            return "Empty";
        } else return f.toString();
    }
    
    public static String getLineageString(Form f){
        StringBuffer buf = new StringBuffer();
        Form[] lineage = ModelWalker.getLineage(f);
        for(int i = 0; i < lineage.length; i++) {
            if (i > 0){
                buf.append("is child of ");
            }
            buf.append(toString(lineage[i]) + '\n');
        }
        return buf.toString();
    }
}
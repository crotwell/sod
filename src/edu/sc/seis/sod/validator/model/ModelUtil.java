/**
 * ModelUtil.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.validator.model;

public class ModelUtil {

    public static String toString(Form f){
        return toString(f, true);
    }

    public static String toString(Form[] f){
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < f.length; i++) {
            buf.append(toString(f[i], false) + ' ');
        }
        return buf.toString();
    }

    public static String toString(Form f, boolean continueFollowing){
        if (f instanceof Attribute) {
            Attribute a = (Attribute)f;
            return "Attribute: " + a.getName();
        } else if (f instanceof MultigenitorForm) {
            StringBuffer buf = new StringBuffer();
            if (f instanceof Choice) {
                buf.append("Choice");
            } else if (f instanceof Interleave){
                buf.append("Interleave");
            } else if (f instanceof Group) {
                buf.append("Group");
            } else {
                buf.append("Unknown MultigenitorForm");
            }
            MultigenitorForm c = (MultigenitorForm)f;
            Form[] kids = c.getChildren();
            if (continueFollowing){
                buf.append(": ");
                buf.append(toString(kids));
            }
            return buf.toString();
        } else if (f instanceof Data) {
            return "Data";
        } else if (f instanceof Empty) {
            return "Empty";
        } else if (f instanceof NamedElement) {
            NamedElement n = (NamedElement)f;
            return "NamedElement: " + n.getName();
        } else if (f instanceof Text) {
            return "Text";
        } else if (f instanceof Value) {
            return "Value";
        } else if (f instanceof NotAllowed) {
            return "NotAllowed";
        } else return f.toString();
    }

}


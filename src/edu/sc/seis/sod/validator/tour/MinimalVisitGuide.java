/*
 * Created on Jul 13, 2004
 */
package edu.sc.seis.sod.validator.tour;

import edu.sc.seis.sod.validator.ModelWalker;
import edu.sc.seis.sod.validator.model.*;

/**
 * @author Charlie Groves
 */
public class MinimalVisitGuide implements TourGuide {
    private Form root;

    public MinimalVisitGuide(Form root) {
        this.root = root;
    }

    public void lead(Tourist visitor) {
        internalLead(visitor, root);
    }

    private void internalLead(Tourist visitor, Form curForm) {
        if (curForm.equals(root) || curForm.getMin() != 0
                || curForm.getAnnotation().getInclude()) {
            if (curForm instanceof NamedElement) {
                NamedElement cur = (NamedElement) curForm;
                visitor.visit(cur);
                internalLead(visitor, cur.getChild());
                visitor.leave(cur);
            } else if (curForm instanceof Choice) {
                Choice c = (Choice) curForm;
                visitor.visit(c);
                Form[] children = c.getChildren();
                for (int i = 0; i < children.length; i++) {
                    if (!ModelWalker.requiresSelfReferentiality(children[i])) {
                        internalLead(visitor, children[i]);
                        break;
                    }
                }
                visitor.leave(c);
            } else if (curForm instanceof Group) {
                Group g = (Group) curForm;
                visitor.visit(g);
                Form[] children = g.getChildren();
                for (int i = 0; i < children.length; i++) {
                    if (!ModelWalker.requiresSelfReferentiality(children[i])) {
                        internalLead(visitor, children[i]);
                    }
                }
                visitor.leave(g);
            } else if (curForm instanceof Interleave) {
                Interleave g = (Interleave) curForm;
                visitor.visit(g);
                Form[] children = g.getChildren();
                for (int i = 0; i < children.length; i++) {
                    if (!ModelWalker.requiresSelfReferentiality(children[i])) {
                        internalLead(visitor, children[i]);
                    }
                }
                visitor.leave(g);
            } else if (curForm instanceof Value) {
                visitor.visit((Value) curForm);
            } else if (curForm instanceof Data) {
                visitor.visit((Data) curForm);
            } else if (curForm instanceof Text) {
                visitor.visit((Text) curForm);
            }else if (curForm instanceof Attribute){
                Attribute attr = (Attribute)curForm;
                visitor.visit(attr);
                internalLead(visitor, attr.getChild());
                visitor.leave(attr);
            }
        }
    }

}
/*
 * Created on Jul 13, 2004
 */
package edu.sc.seis.sod.validator.tour;

import edu.sc.seis.sod.validator.ModelWalker;
import edu.sc.seis.sod.validator.model.Attribute;
import edu.sc.seis.sod.validator.model.Choice;
import edu.sc.seis.sod.validator.model.Data;
import edu.sc.seis.sod.validator.model.Form;
import edu.sc.seis.sod.validator.model.Group;
import edu.sc.seis.sod.validator.model.Interleave;
import edu.sc.seis.sod.validator.model.MultigenitorForm;
import edu.sc.seis.sod.validator.model.NamedElement;
import edu.sc.seis.sod.validator.model.Text;
import edu.sc.seis.sod.validator.model.Value;

/**
 * @author Charlie Groves
 * 
 * Takes a tourist that it leads through all of the children of the root node.
 * The path taken may not even be valid, so be forewarned
 */
public class MaximalVisitGuide implements TourGuide {

    public MaximalVisitGuide(Form root) {
        this.root = root;
    }

    public void lead(Tourist visitor) {
        internalLead(visitor, root);
    }

    private void internalLead(Tourist visitor, Form curForm) {
        if (!ModelWalker.lineageContainsRefTo(curForm, curForm.getDef(), root)) {
            if (curForm instanceof NamedElement) {
                NamedElement cur = (NamedElement) curForm;
                visitor.visit(cur);
                internalLead(visitor, cur.getChild());
                visitor.leave(cur);
            } else if (curForm instanceof Choice) {
                Choice c = (Choice) curForm;
                visitor.visit(c);
                handleKids(visitor, c);
                visitor.leave(c);
            } else if (curForm instanceof Group) {
                Group g = (Group) curForm;
                visitor.visit(g);
                handleKids(visitor, g);
                visitor.leave(g);
            } else if (curForm instanceof Interleave) {
                Interleave g = (Interleave) curForm;
                visitor.visit(g);
                handleKids(visitor, g);
                visitor.leave(g);
            } else if (curForm instanceof Value) {
                visitor.visit((Value) curForm);
            } else if (curForm instanceof Data) {
                visitor.visit((Data) curForm);
            } else if (curForm instanceof Text) {
                visitor.visit((Text) curForm);
            } else if (curForm instanceof Attribute) {
                Attribute attr = (Attribute) curForm;
                visitor.visit(attr);
                internalLead(visitor, attr.getChild());
                visitor.leave(attr);
            }
        } else {
            MinimalVisitGuide min = new MinimalVisitGuide(curForm);
            min.lead(visitor);
        }
    }

    private void handleKids(Tourist visitor, MultigenitorForm f) {
        Form[] children = f.getChildren();
        for (int i = 0; i < children.length; i++) {
            internalLead(visitor, children[i]);
        }
    }

    private Form root;

}
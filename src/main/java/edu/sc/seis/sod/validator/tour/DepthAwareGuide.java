/*
 * Created on Jul 15, 2004
 */
package edu.sc.seis.sod.validator.tour;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.sc.seis.sod.validator.model.Attribute;
import edu.sc.seis.sod.validator.model.Choice;
import edu.sc.seis.sod.validator.model.Data;
import edu.sc.seis.sod.validator.model.Definition;
import edu.sc.seis.sod.validator.model.Form;
import edu.sc.seis.sod.validator.model.Group;
import edu.sc.seis.sod.validator.model.Interleave;
import edu.sc.seis.sod.validator.model.MultigenitorForm;
import edu.sc.seis.sod.validator.model.NamedElement;
import edu.sc.seis.sod.validator.model.Text;
import edu.sc.seis.sod.validator.model.Value;

/**
 * @author Charlie Groves
 */
public class DepthAwareGuide implements TourGuide {

    public DepthAwareGuide(Form root) {
        this.root = root;
        if(root instanceof MultigenitorForm) {
            allowableDepth = 1;
        }
    }

    public void lead(Tourist visitor) {
        internalLead(visitor, root);
    }

    public void internalLead(Tourist visitor, Form curForm) {
        if(calcDistance(curForm) > allowableDepth
                && curForm.getParent().isFromDef()
                && (!(curForm instanceof Value))) { return; }
        if(curForm instanceof NamedElement) {
            NamedElement cur = (NamedElement)curForm;
            visitor.visit(cur);
            internalLead(visitor, cur.getChild());
            visitor.leave(cur);
        } else if(curForm instanceof Choice) {
            Choice c = (Choice)curForm;
            visitor.visit(c);
            handleKids(visitor, c, true);
            visitor.leave(c);
        } else if(curForm instanceof Group) {
            Group g = (Group)curForm;
            visitor.visit(g);
            handleKids(visitor, g, false);
            visitor.leave(g);
        } else if(curForm instanceof Interleave) {
            Interleave g = (Interleave)curForm;
            visitor.visit(g);
            handleKids(visitor, g, true);
            visitor.leave(g);
        } else if(curForm instanceof Value) {
            visitor.visit((Value)curForm);
        } else if(curForm instanceof Data) {
            visitor.visit((Data)curForm);
        } else if(curForm instanceof Text) {
            visitor.visit((Text)curForm);
        } else if(curForm instanceof Attribute) {
            Attribute attr = (Attribute)curForm;
            visitor.visit(attr);
            internalLead(visitor, attr.getChild());
            visitor.leave(attr);
        }
    }

    private void handleKids(Tourist visitor, MultigenitorForm f, boolean sort) {
        Form[] children = f.getChildren();
        List<Form> tosort = Arrays.asList(children);
        if (sort) {
            Collections.sort(tosort, new Comparator<Form>() {
                @Override
                public int compare(Form o1, Form o2) {
                    if (o1.isFromDef() && o2.isFromDef()) {
                        return o1.getDef().getName().toLowerCase().compareTo(o2.getDef().getName().toLowerCase());
                    } else {
                        return 0;
                    }
                }
                
            });
        }
        for (Form form : tosort) {
            internalLead(visitor, form);
        }
    }

    public int calcDistance(Form f) {
        if(f.equals(root)) { return 0; }
        return calcDistance(f.getParent()) + 1;
    }

    int allowableDepth = 2;

    private Form root;
}
/**
 * ExampleBuilder.java
 * 
 * @author Created by Philip Oliver-Paull
 */
package edu.sc.seis.sod.validator.example;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import edu.sc.seis.sod.validator.ModelWalker;
import edu.sc.seis.sod.validator.model.Annotation;
import edu.sc.seis.sod.validator.model.Attribute;
import edu.sc.seis.sod.validator.model.Choice;
import edu.sc.seis.sod.validator.model.Data;
import edu.sc.seis.sod.validator.model.DataList;
import edu.sc.seis.sod.validator.model.Empty;
import edu.sc.seis.sod.validator.model.Form;
import edu.sc.seis.sod.validator.model.Group;
import edu.sc.seis.sod.validator.model.Interleave;
import edu.sc.seis.sod.validator.model.ModelUtil;
import edu.sc.seis.sod.validator.model.MultigenitorForm;
import edu.sc.seis.sod.validator.model.NamedElement;
import edu.sc.seis.sod.validator.model.Text;
import edu.sc.seis.sod.validator.model.Value;

public class ExampleBuilder {

    public ExampleBuilder(boolean htmlReadable) {
        if(htmlReadable) {
            openBracket = "&lt;";
            closeBracket = "&gt;";
        } else {
            openBracket = "<";
            closeBracket = ">";
        }
    }

    public ExampleBuilder(String openBracket, String closeBracket) {
        this.openBracket = openBracket;
        this.closeBracket = closeBracket;
    }

    public void setRequiredExample(Annotation ann) {
        this.requiredExample = ann;
        System.out.println("REQUIRED EXAMPLE SET: "
                + ModelUtil.toString(ann.getFormProvider().getForm()));
        //System.out.println("Lineage: " +
        // ModelUtil.getLineageString(ann.getFormProvider().getForm()));
    }

    public boolean isExampleInserted() {
        return exampleInserted;
    }

    public void write(Form f) {
        write(f, true);
    }

    public void write(Form f, boolean ignoreMin) {
        if(requiredExample != null) {
            if(f instanceof NamedElement
                    && ((NamedElement)f).getName().equals("OriginOR")) {
                System.out.println("f is OriginOR!");
                Form ex = requiredExample.getFormProvider().getForm();
                if(ex instanceof NamedElement
                        && ((NamedElement)f).getName().equals("OriginOR")) {
                    System.out.println("OriginOR should be inserted, damn it");
                }
            }
        }
        if(f.getMin() == 0 && !ignoreMin && !f.getAnnotation().getInclude()) {
            //System.out.println("f.getMin() == 0 && !ignoreMin &&
            // !f.getAnnotation().getInclude()");
            if(requiredExample == null
                    || !isTowardsOrInLineage(f,
                                             requiredExample.getFormProvider()
                                                     .getForm())) {
                //System.out.println("requiredExample == null ||
                // !ModelWalker.isTowards(f,
                // requiredExample.getFormProvider().getForm())");
                return;
            }
        }
        if(requiredExample != null
                && f.equals(requiredExample.getFormProvider().getForm())) {
            System.out.println("REQUIRED EXAMPLE INSERTED: "
                    + ModelUtil.toString(f));
            exampleInserted = true;
            buf.append(requiredExample.getExample(false));
            return;
        }
        //this attribute stuff is wrong. Don't worry right now
        if(f instanceof Attribute) {
            //System.out.println("f instanceof Attribute");
            Attribute attr = (Attribute)f;
            if(!attrQueue.contains(attr)) {
                attrQueue.add(attr); //this gets rid of double processing of
                // attributes
                buf.append(' ' + attr.getName() + "=\"");
                Form kid = attr.getChild();
                if(kid != null) {
                    write(kid, false);
                }
                buf.append('\"');
            } else {
                attrQueue.remove(attr);
            }
        } else if(f instanceof Choice) {
            //System.out.println("f instanceof Choice");
            Choice c = (Choice)f;
            Form[] children = c.getChildren();
            //System.out.println("choice is: " + ModelUtil.toString(c));
            if(requiredExample != null
                    && isTowardsOrInLineage(f,
                                            requiredExample.getFormProvider()
                                                    .getForm())) {

                System.out.println("Found choice towards required");
                for(int i = 0; i < children.length; i++) {
                    if(isTowardsOrInLineage(children[i],
                                            requiredExample.getFormProvider()
                                                    .getForm())) {
                        /*System.out.println("istowards chosen "
                                + ModelUtil.toString(children[i]));*/
                        write(children[i], true);
                        break;
                    }
                }
            } else {
                System.out.println("Found choice not towards required");
                for(int i = 0; i < children.length; i++) {
                    if(!ModelWalker.requiresSelfReferentiality(children[i])) {
                        //System.out.println("otherwise chosen " +
                        // ModelUtil.toString(children[i]));
                        write(children[i], false);
                        break;
                    }
                }
            }
        } else if(f instanceof Value) {
            //System.out.println("f instanceof Value");
            buf.append(((Value)f).getValue());
        } else if(f instanceof Data) {
            //System.out.println("f instanceof Data");
            if(f.getAnnotation().hasExampleFromAnnotation()) {
                buf.append(f.getAnnotation().getExample(false));
            } else {
                buf.append(DEFAULT_INT_VALUE);
            }
        } else if(f instanceof DataList) {} else if(f instanceof Group
                || f instanceof Interleave) {
            //System.out.println("f instanceof Group || f instanceof
            // Interleave");
            MultigenitorForm m = (MultigenitorForm)f;
            Form[] kids = m.getChildren();
            for(int i = 0; i < kids.length; i++) {
                if(requiredExample != null && f.equals(kids[i])) {
                    write(kids[i], true);
                } else {
                    write(kids[i], false);
                }
            }
        } else if(f instanceof NamedElement) {
            //System.out.println("f instanceof NamedElement");
            NamedElement ne = (NamedElement)f;
            buf.append(openBracket + ne.getName());
            if(ne.getAttributes() != null) {
                Attribute[] attrs = ne.getAttributes();
                for(int i = 0; i < attrs.length; i++) {
                    write(attrs[i], false);
                }
            }
            if(ne.getChild() instanceof Empty
                    && !(ne.getChild() instanceof Text)) {
                //System.out.println("ne.getChild() instanceof Empty");
                buf.append(" /" + closeBracket + '\n');
            } else {
                buf.append(closeBracket);
                Form kid = ne.getChild();
                if(kid != null) {
                    write(kid, false);
                }
                buf.append(openBracket + '/' + ne.getName() + closeBracket
                        + '\n');
            }
        } else if(f instanceof Text) {
            //System.out.println("f instanceof Text");
            buf.append(DEFAULT_TEXT_VALUE);
        } else if(f instanceof Empty) {}
    }

    public static boolean isTowardsOrInLineage(Form parent, Form result) {
        //return ModelWalker.isTowards(parent, result)
        //        || ModelWalker.isInLineage(parent, result);
        return ModelWalker.isInLineage(parent, result);
    }

    public String toString() {
        return buf.toString();
    }

    public static String getNamespacePrefix(String nsURL) {
        StringTokenizer tok = new StringTokenizer(nsURL, "/");
        String prefix = null;
        while(tok.hasMoreTokens()) {
            prefix = tok.nextToken();
        }
        return prefix;
    }

    private StringBuffer buf = new StringBuffer();

    private List attrQueue = new ArrayList();

    private String openBracket, closeBracket;

    private Annotation requiredExample = null;

    private boolean exampleInserted = false;

    public static final String DEFAULT_TEXT_VALUE = "text";

    public static final int DEFAULT_INT_VALUE = 12;
}
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
        this.requiredForm = ann.getFormProvider().getForm();
    }

    public boolean isExampleInserted() {
        return exampleInserted;
    }

    public void write(Form f) {
        write(f, true);
    }

    public void write(Form f, boolean ignoreMin) {
        if(f.getMin() == 0 && !ignoreMin && !f.getAnnotation().getInclude()
                && !ModelWalker.isTowards(f, requiredForm)){ 
            return; 
            }
        if(f.equals(requiredForm)) {
            exampleInserted = true;
            buf.append(requiredExample.getExample(false));
            requiredExample = null;
            requiredForm = null;
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
            Choice c = (Choice)f;
            Form[] children = c.getChildren();
            if(ModelWalker.getDistance(f, requiredForm) != -1) {
                int minDist = Integer.MAX_VALUE;
                Form chosenChild = null;
                for(int i = 0; i < children.length; i++) {
                    int curDist = ModelWalker.getDistance(children[i],
                                                          requiredForm);
                    if(curDist > -1 && curDist < minDist) {
                        minDist = curDist;
                        chosenChild = children[i];
                    }
                }
                write(chosenChild, true);
            } else {
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
            buf.append(((Value)f).getValue());
        } else if(f instanceof Data) {
            if(f.getAnnotation().hasExampleFromAnnotation()) {
                buf.append(f.getAnnotation().getExample(false));
            } else {
                buf.append(DEFAULT_INT_VALUE);
            }
        } else if(f instanceof DataList) {} else if(f instanceof Group
                || f instanceof Interleave) {
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
            buf.append(DEFAULT_TEXT_VALUE);
        } else if(f instanceof Empty) {}
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

    private Form requiredForm;

    private boolean exampleInserted = false;

    public static final String DEFAULT_TEXT_VALUE = "text";

    public static final int DEFAULT_INT_VALUE = 12;
}
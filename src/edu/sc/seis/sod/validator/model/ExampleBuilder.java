/**
 * ExampleBuilder.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.validator.ModelWalker;
import java.util.ArrayList;
import java.util.List;

public class ExampleBuilder {

    public ExampleBuilder(boolean htmlReadable){
        if (htmlReadable){
            openBracket = "&lt;";
            closeBracket = "&gt;";
        }
        else {
            openBracket = "<";
            closeBracket = ">";
        }
    }

    public ExampleBuilder(String openBracket, String closeBracket){
        this.openBracket = openBracket;
        this.closeBracket = closeBracket;
    }

    public void write(Form f){
        write(f, true);
    }

    public void write(Form f, boolean ignoreMin){
        //System.out.println(ModelUtil.toString(f));
        if (f.getMin() == 0 && !ignoreMin && !f.getAnnotation().getInclude()) {
            return;
        }
        //this attribute stuff is wrong.  Don't worry right now
        if (f instanceof Attribute){
            Attribute attr = (Attribute)f;
            if (!attrQueue.contains(attr)){
                attrQueue.add(attr); //this gets rid of double processing of attributes
                buf.append(' ' + attr.getName() + "=\"");
                Form kid = attr.getChild();
                if (kid != null){
                    write(kid, false);
                }
                buf.append('\"');
            }
            else {
                attrQueue.remove(attr);
            }
        } else if (f instanceof Choice){
            Choice c = (Choice)f;
            Form[] children = c.getChildren();
            for (int i = 0; i < children.length; i++) {
                if (!ModelWalker.requiresSelfReferentiality(children[i])){
                    write(children[i], false);
                    break;
                }
            }
        } else if (f instanceof Value){
            buf.append(((Value)f).getValue());
        } else if (f instanceof Data){
            buf.append(DEFAULT_INT_VALUE);
        } else if (f instanceof DataList) {
        } else if (f instanceof Group || f instanceof Interleave) {
            MultigenitorForm m = (MultigenitorForm)f;
            Form[] kids = m.getChildren();
            for (int i = 0; i < kids.length; i++) {
                write(kids[i], false);
            }
        } else if (f instanceof NamedElement) {
            NamedElement ne = (NamedElement)f;
            buf.append(openBracket + ne.getName());
            if (ne.getAttributes() != null){
                Attribute[] attrs = ne.getAttributes();
                for (int i = 0; i < attrs.length; i++) {
                    write(attrs[i], false);
                }
            }
            buf.append(closeBracket);
            Form kid = ne.getChild();
            if (kid != null){
                write(kid, false);
            }
            buf.append(openBracket + '/' + ne.getName() + closeBracket + '\n');
        } else if (f instanceof Text) {
            buf.append(DEFAULT_TEXT_VALUE);
        } else if (f instanceof Empty) {
        }
    }

    public String toString(){
        return buf.toString();
    }

    private StringBuffer buf = new StringBuffer();
    private List attrQueue = new ArrayList();
    private String openBracket, closeBracket;

    public static final String DEFAULT_TEXT_VALUE = "text";
    public static final int DEFAULT_INT_VALUE = 12;
}


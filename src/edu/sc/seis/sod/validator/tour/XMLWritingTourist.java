/*
 * Created on Jul 13, 2004
 */
package edu.sc.seis.sod.validator.tour;

import edu.sc.seis.sod.validator.model.*;

/**
 * @author Charlie Groves
 */
public class XMLWritingTourist implements Tourist {

    public XMLWritingTourist() {
        this(false);
    }

    public XMLWritingTourist(boolean escapeBrackets) {
        if(escapeBrackets) {
            open = "&lt;";
            close = "&gt;";
        }
    }

    private Form preAttrForm;

    public void visit(Attribute attr) {
        replaceClose("");
        write(" " + attr.getName() + "=\"");
        preAttrForm = lastForm;
    }

    public void leave(Attribute attr) {
        write("\"" + close);
        lastForm = preAttrForm;
    }

    public void visit(Data d) {
        lastForm = d;
        if(d.getAnnotation().hasExampleFromAnnotation()) {
            write(d.getAnnotation().getExample());
        } else {
            write(d.getDatatype().getExampleValue());
        }
    }

    public void visit(NamedElement ne) {
        if(!ne.equals(lastForm) && lastForm != null && !leftLast) {
            write("\n");
        }
        if(waitToLeave == null && ne.getAnnotation().hasExampleFromAnnotation()) {
            String example = ne.getAnnotation().getExample(true);
            indentAndWrite(example.replaceAll("\n", "\n" + getCurIndent())
                    + "\n");
            waitToLeave = ne;
        } else {
            indentAndWrite(open + ne.getName() + close);
            lastForm = ne;
            leftLast = false;
        }
        depth++;
    }

    public void leave(NamedElement ne) {
        depth--;
        if(ne.equals(lastForm)) {
            replaceClose("/" + close + "\n");
        } else {
            indentAndWrite(open + "/" + ne.getName() + close + "\n");
        }
        leftLast = true;
        if(ne.equals(waitToLeave)) {
            waitToLeave = null;
        }
    }

    private void indentAndWrite(String text) {
        write(getCurIndent() + text);
    }

    private String getCurIndent() {
        StringBuffer indent = new StringBuffer();
        if(result.length() > 0 && result.charAt(result.length() - 1) == '\n') {
            for(int i = 0; i < depth; i++) {
                indent.append("  ");
            }
        }
        return indent.toString();
    }

    private void write(String text) {
        if(waitToLeave == null) {
            result.append(text);
        }
    }

    private void replaceClose(String with) {
        if(waitToLeave == null) {
            result.replace(result.length() - close.length(),
                           result.length(),
                           with);
        }
    }

    public void visit(Text t) {
        lastForm = t;
        if(t.getAnnotation().hasExampleFromAnnotation()) {
            write(t.getAnnotation().getExample());
        } else {
            write(DEFAULT_TEXT_VALUE);
        }
    }

    public void visit(Value v) {
        lastForm = v;
        write(v.getValue());
    }

    public String getResult() {
        return result.toString();
    }

    public void visit(Choice choice) {}

    public void leave(Choice choice) {}

    public void visit(Empty e) {}

    public void visit(Group g) {}

    public void leave(Group g) {}

    public void visit(Interleave i) {}

    public void leave(Interleave i) {}

    public void visit(NotAllowed na) {}

    private StringBuffer result = new StringBuffer();

    private boolean leftLast;

    int depth = 0;

    private Form lastForm;

    private Form waitToLeave;

    public static final int DEFAULT_INT_VALUE = 12;

    public static final String DEFAULT_TEXT_VALUE = "text";

    private String open = "<";

    private String close = ">";
}
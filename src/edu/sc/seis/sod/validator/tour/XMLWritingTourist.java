/*
 * Created on Jul 13, 2004
 */
package edu.sc.seis.sod.validator.tour;

import edu.sc.seis.sod.validator.model.*;

/**
 * @author Charlie Groves
 */
public class XMLWritingTourist implements Tourist{
    public XMLWritingTourist(){
        this(false);
    }

    public XMLWritingTourist(boolean escapeBrackets){
        if(escapeBrackets){
            open = "&lt;";
            close = "&gt;";
        }
    }

    private Form preAttrForm;

    public void visit(Attribute attr){
        result.replace(result.length() - close.length(), result.length(), "");
        result.append(" " + attr.getName() + "=\"");
        preAttrForm = lastForm;
    }

    public void leave(Attribute attr){
        result.append("\"" + close);
        lastForm = preAttrForm;
    }

    public void visit(Choice choice){}

    public void leave(Choice choice){}

    public void visit(Data d){
        lastForm = d;
        if(d.getAnnotation().hasExampleFromAnnotation()){
            result.append(d.getAnnotation().getExample());
        } else{
            result.append(d.getDatatype().getExampleValue());
        }
    }

    public void visit(Empty e){}

    public void visit(Group g){}

    public void leave(Group g){}

    public void visit(Interleave i){}

    public void leave(Interleave i){}

    public void visit(NamedElement ne){
        if(!ne.equals(lastForm) && lastForm != null && !leftLast){
            result.append("\n");
        }
        write(open + ne.getName() + close);
        lastForm = ne;
        leftLast = false;
        depth++;
    }

    public void leave(NamedElement ne){
        depth--;
        if(ne.equals(lastForm)){
            result.replace(result.length() - close.length(), result.length(),
                    " /" + close + "\n");
        } else{
            write(open + "/" + ne.getName() + close + "\n");
        }
        leftLast = true;
    }

    private void write(String text){
        if(result.length() > 0 && result.charAt(result.length() - 1) == '\n'){
            for(int i = 0; i < depth; i++){
                result.append("  ");
            }
        }
        result.append(text);
    }

    public void visit(Text t){
        lastForm = t;
        result.append(DEFAULT_TEXT_VALUE);
    }

    public void visit(Value v){
        lastForm = v;
        result.append(v.getValue());
    }

    public void visit(NotAllowed na){}

    public String getResult(){
        return result.toString();
    }

    private StringBuffer result = new StringBuffer();
    private boolean leftLast;
    int depth = 0;
    private Form lastForm;
    public static final int DEFAULT_INT_VALUE = 12;
    public static final String DEFAULT_TEXT_VALUE = "text";
    private String open = "<";
    private String close = ">";
}
/**
 * Start.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;
import org.xml.sax.Attributes;

public abstract class Start implements Pattern{
    public Start(Grammar parent){ this.parent = parent; }

    public static Start createStart(Grammar owner, Attributes attributes) {
        String combineType = attributes.getValue("combine");
        if(combineType == null){ return createStart(owner, "");}
        else{ return createStart(owner, combineType); }
    }
    public static Start createStart(Grammar owner, Start type) {
        if(type instanceof NoCombineStart){ return createStart(owner, ""); }
        else if(type instanceof ChoiceStart){ return createStart(owner, "choice"); }
        else { return createStart(owner, "interleave"); }
    }

    private static Start createStart(Grammar owner, String combineType){
        if(combineType == ""){ return new NoCombineStart(owner); }
        else if(combineType.equals("choice")){ return new ChoiceStart(owner); }
        else { return new InterleaveStart(owner); }
    }

    public Pattern[] getKids() { return new Pattern[]{ el }; }

    public abstract void combineWith(Start otherStart);

    public void addChild(Pattern child) {
        if(el == null){ el = child; }
        else { throwTooManyKids(); }
    }

    public void addChildren(Pattern[] children) {
        if(children.length == 1){ addChild(children[0]); }
        else{ throwTooManyKids(); }
    }

    protected void throwTooManyKids(){
        throw new UnsupportedOperationException(this + " can only accept one child pattern");
    }

    public Pattern getChild(){ return el; }

    public Pattern getParent() { return parent; }

    public void dereference(){
        if(el instanceof Definition){ el = el.getKids()[0]; }
        el.dereference();
    }

    private Grammar parent;
    private Pattern el;
}


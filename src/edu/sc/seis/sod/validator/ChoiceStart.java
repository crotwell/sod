/**
 * ChoiceStart.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;

public class ChoiceStart extends Start {
    public ChoiceStart(Grammar owner){
        super(owner);
        super.addChild(new Choice(owner, null));
        this.owner = owner;
    }

    public void combineWith(Start otherStart) {
        if(otherStart instanceof ChoiceStart){
            ChoiceStart otherChoiceStart = (ChoiceStart)otherStart;
            getChoice().addChildren(otherChoiceStart.getChoice().getKids());
        }else{
            getChoice().addChild(otherStart.getChild());
        }
    }

    public void addChild(Pattern def) {
        if(getChoice().getKids().length == 0){ getChoice().addChild(def); }
        else{ throwTooManyKids(); }
    }

    public String toString(){ return "Choice start for " + owner; }

    public Choice getChoice(){ return (Choice)getChild(); }

    private Grammar owner;
}


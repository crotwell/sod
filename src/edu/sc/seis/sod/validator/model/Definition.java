/**
 * Definition.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public class Definition{
    public Definition(String name, Grammar containingGrammar){
        this(name, containingGrammar, UNDEFINED);
    }

    public Definition(String name, Grammar containingGrammar, int combine){
        this.name = name;
        this.combine = combine;
        this.grammar = containingGrammar;
    }

    public Definition combineWith(Definition d) {
        //TODO
        return null;
    }

    public void set(FormProvider f){
        form = f;
        if(form instanceof Form){
            form = ((Form)form).deref(null, this);
        }
    }

    public Form getForm() { return form.getForm(); }

    public String getName(){ return name; }

    public int getCombine(){ return combine; }

    public Grammar getGrammar(){ return grammar; }

    private int combine;

    //Combine types
    public static final int UNDEFINED = 0, CHOICE = 1, INTERLEAVE = 2;

    private FormProvider form;
    private String name;
    private Grammar grammar;
}


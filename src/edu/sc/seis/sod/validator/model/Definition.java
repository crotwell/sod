/**
 * Definition.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public class Definition{
    public Definition(String name){ this(name, UNDEFINED); }

    public Definition(String name, int combine){
        this.name = name;
        this.combine = combine;
    }

    public Definition combineWith(Definition d) {
        //TODO
        return null;
    }

    public void set(FormProvider f){  form = f; }

    public Form getForm() { return form.getForm(); }

    public String getName(){ return name; }

    public int getCombine(){ return combine; }

    public boolean equals(Object o){
        if(o == this){ return true; }
        if(o instanceof Definition){
            Definition otherDef = (Definition)o;
            return otherDef.getForm().equals(getForm()) &&
                otherDef.getName().equals(getName());
        }
        return false;
    }

    public int hashCode(){
        return 37 * getForm().hashCode() + 37 * getName().hashCode();
    }

    private int combine;

    //Combine types
    public static final int UNDEFINED = 0, CHOICE = 1, INTERLEAVE = 2;

    private FormProvider form;
    private String name;
}


/**
 * RelaxGrammar.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Grammar implements Pattern{
    public void addChild(Pattern child) {
        if(child instanceof Definition){ addDefinition((Definition)child); }
        else if(child instanceof Start){ addStart((Start)child); }
        else {
            throw new UnsupportedOperationException("You attempted to add " + child + " to " + this + ".  Only Start and Definition elements can belong to a grammar");
        }
    }

    public void addChildren(Pattern[] children) {
        for (int i = 0; i < children.length; i++) { addChild(children[i]); }
    }

    public Pattern[] getKids() {
        Pattern[] definitions = new Pattern[definedElements.size()];
        definedElements.toArray(definitions);
        Pattern[] startArray = new Pattern[starts.size()];
        starts.toArray(startArray);
        Pattern[] kids = new Pattern[startArray.length + definitions.length];
        System.arraycopy(definitions, 0, kids, 0, definitions.length);
        System.arraycopy(startArray, 0, kids, definitions.length, startArray.length);
        return kids;
    }

    public void dereference() {
        if(starts.size() > 1){
            Iterator it = starts.iterator();
            Class startType = null;
            while(it.hasNext()){
                Start cur = (Start)it.next();
                if(cur instanceof ChoiceStart){
                    if(startType == null){ startType = ChoiceStart.class; }
                    else if(startType != ChoiceStart.class){
                        throw new IllegalArgumentException("Conflicting combine types in " + this);
                    }
                }else if(cur instanceof InterleaveStart){
                    if(startType == null){ startType = InterleaveStart.class; }
                    else if(startType != InterleaveStart.class){
                        throw new IllegalArgumentException("Conflicting combine types in " + this);
                    }
                }
            }
            if(startType == null){
                throw new IllegalArgumentException("Several Start elements specified in " + this + " but no combine type was specified");
            }
            if(startType == ChoiceStart.class){
                start = new ChoiceStart(this);
            }else{
                start = new InterleaveStart(this);
            }
            it = starts.iterator();
            while(it.hasNext()){ start.combineWith((Start)it.next()); }
        }else{
            start = (Start)starts.get(0);
        }
        Pattern[] kids = getKids();
        for (int i = 0; i < kids.length; i++) { kids[i].dereference();}
    }

    public void addStart(Start startElement){
        if(start == null){starts.add(startElement);}
    }

    public Start getStart(){ return start; }

    public Definition getDefinition(String name){
        Definition[] defs = getDefinitions();
        for (int i = 0; i < defs.length; i++) {
            if(defs[i].getName().equals(name)){ return defs[i]; }
        }
        return null;
    }

    public Definition[] getDefinitions(){
        return (Definition[])definedElements.toArray(new Definition[0]);
    }

    public void addDefinition(Definition el){
        definedElements.add(el);
        Iterator it = waitingReferences.iterator();
        while(it.hasNext()){
            Reference cur = (Reference)it.next();
            if(cur.getName().equals(el.getName())){
                cur.setDefinition(el);
                it.remove();
            }
        }
    }

    public void include(Grammar includedGrammar){
        include(includedGrammar, includedGrammar.getStart(), new Definition[0]);
    }

    public void include(Grammar includedGrammar, Start includedStart,
                        Definition[] redefinitions){
        addStart(includedStart);
        addChildren(redefinitions);
        Definition[] includedEls = includedGrammar.getDefinitions();
        for (int i = 0; i < includedEls.length; i++) {
            boolean alreadyRedefined = false;
            for (int j = 0; j < redefinitions.length && !alreadyRedefined; j++) {
                if(includedEls[i].getName().equals(redefinitions[j].getName())){
                    alreadyRedefined = true;
                }
            }
            if(!alreadyRedefined){ addChild(includedEls[i]); }
        }
    }

    public Definition handleReference(String refName) {
        Definition def = getDefinition(refName);
        if(def != null){ return def; }
        else{
            Reference ref = new Reference(this, refName);
            waitingReferences.add(ref);
            return ref;
        }
    }

    private List waitingReferences = new ArrayList();
    private List definedElements = new ArrayList();
    private Start start;
    private List starts = new ArrayList();
}

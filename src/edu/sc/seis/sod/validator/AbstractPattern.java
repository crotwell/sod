/**
 * AbstractStructuralElement.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPattern  implements Pattern{
    public AbstractPattern(Grammar owner, Pattern parent){
        this.parent = parent;
        this.owner = owner;
    }

    public void addChild(Pattern child) {
        kids.add(child);
    }

    public void addChildren(Pattern[] children) {
        addChildren(children, this.kids.size());
    }

    public void addChildren(Pattern[] children, int position) {
        for (int i = 0; i < children.length; i++) {
            this.kids.add(position++, children[i]);
        }
    }

    public Pattern[] getKids() {
        return (Pattern[])kids.toArray(new Pattern[0]);
    }

    public Pattern getParent() { return parent; }

    //Dereferencing a pattern only needs to be done once since it gets all of
    //its children.  The dereferenced boolean ensures that if this pattern is
    //referenced by its children, it doesn't get caught in a dereferencing loop.
    public void dereference() {
        if(!dereferenced){
            dereferenced = true;
            for (int i = 0; i < kids.size(); i++) {
                if(kids.get(i) instanceof Definition){
                    Definition ref = (Definition)kids.remove(i);
                    Pattern[] newKids = ref.getKids();
                    addChildren(newKids, i);
                    i--;
                }else{ ((Pattern)kids.get(i)).dereference(); }
            }
        }
    }

    private List kids = new ArrayList();
    private Pattern parent;
    private Grammar owner;
    private boolean dereferenced= false;
}

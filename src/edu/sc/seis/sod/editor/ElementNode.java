/**
 * ElementNode.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class ElementNode implements Serializable{
    public ElementNode(String name) {
        this.name = name;
    }

    public void addParent(ElementNode parent) {
        parents.add(parent);
    }

    public ElementNode[] getParents() {
        return (ElementNode[])parents.toArray(new ElementNode[0]);
    }

    public String getName() {
        return name;
    }

    public void addChild(ElementNode child) {
        child.addParent(this);
        children.put(child.getName(), child);
    }

    public boolean containsChild(ElementNode child) {
        return children.containsKey(child.getName());
    }

    public Iterator childIterator() {
        return children.values().iterator();
    }

    HashMap children = new HashMap();
    String name;
    LinkedList parents = new LinkedList();

    public String getFullName() {
        if (parents.size() != 0) {
            return ((ElementNode)parents.get(0)).getFullName()+"/"+getName();
        } else {
            return getName();
        }
    }

    public String toString() {
        return getFullName();
    }
}



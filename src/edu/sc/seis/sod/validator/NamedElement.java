package edu.sc.seis.sod.validator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NamedElement extends AbstractPattern{
    public NamedElement(Grammar owner, Pattern parent, String name){
        super(owner, parent);
        this.name = name;
    }

    public String getName(){ return name; }

    public String toString(){ return "NamedElement: " + name; }

    private String name;
}


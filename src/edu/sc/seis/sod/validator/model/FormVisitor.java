/**
 * FormVisitor.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public interface FormVisitor{
    public void visit(Attribute attr);
    public void leave(Attribute attr);
    public void visit(Choice choice);
    public void leave(Choice choice);
    public void visit(Data d);
    public void visit(Empty e);
    public void visit(Group g);
    public void leave(Group g);
    public void visit(Interleave i);
    public void leave(Interleave i);
    public void visit(NamedElement ne);
    public void leave(NamedElement ne);
    public void visit(Text t);
    public void visit(Value v);
    public void visit(NotAllowed na);
}


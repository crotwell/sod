/**
 * FormPrinter.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public class FormPrinter implements FormVisitor{
    public FormPrinter(){ this(2); }

    public FormPrinter(int indentLevel){
        for (int i = 0; i < indentLevel; i++){ this.indent += ' '; }
    }

    public void visit(Attribute attr) {
        String print = "@" + attr.getName() + " " + getCardinality(attr);
        if(attr.getAnnotation() != null){
            print += " " + attr.getAnnotation().getSummary();
        }
        printAndIncreaseDepth(print);
    }

    public void visit(Choice choice) {
        printAndIncreaseDepth("Choice " + getCardinality(choice));
    }

    private String getCardinality(Form choice) {
        return "Min: " + choice.getMin() + " Max: " + choice.getMax();
    }

    public void visit(Data d) {
        print("Data: " + d.getDatatype() + " " + getCardinality(d));
    }

    public void visit(Empty e) { print("Empty"); }

    public void visit(Group g) {
        printAndIncreaseDepth("Group" + " " + getCardinality(g));
    }

    public void visit(Interleave i) {
        printAndIncreaseDepth("Interleave" + " " + getCardinality(i));
    }

    public void visit(NotAllowed na) { print("Not Allowed"); }


    public void visit(NamedElement ne) {
        String print = ne.getName() + " " + getCardinality(ne);
        if(ne.getAnnotation() != null){
            print += " " + ne.getAnnotation().getSummary();
        }
        printAndIncreaseDepth(print);
    }

    public void visit(Text t) { print("Text"); }

    public void leave(Attribute attr) { depth--; }

    public void leave(Choice choice) { depth--; }

    public void leave(Group g) { depth--; }

    public void leave(Interleave i) { depth--; }

    public void leave(NamedElement ne) { depth--; }

    public void visit(Value v) {
        print("Value: " + v.getValue() + " " + v.getDatatype() + " " + getCardinality(v));
    }

    private void printAndIncreaseDepth(String s) {
        print(s);
        depth++;
    }
    private void print(String s){
        for (int i = 0; i < depth; i++) { System.out.print(indent); }
        System.out.println(s);
    }

    private int depth = 0;
    private String indent = "";
}


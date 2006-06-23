package edu.sc.seis.sod.status;

public class Pass extends StringTreeLeaf {

    public Pass(Object subsetter) {
        this(subsetter, "");
    }
    
    public Pass(Object subsetter, String reason){
        super(subsetter, true, reason);
    }
}

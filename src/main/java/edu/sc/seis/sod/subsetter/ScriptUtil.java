package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;


public class ScriptUtil {
    
    public ScriptUtil(Subsetter subsetter) {
        this.subsetter = subsetter;
    }
    
    public Pass pass() {
        return new Pass(subsetter);
    }
    
    public Pass pass(String reason) {
        return new Pass(subsetter, reason);
    }
    
    public Fail fail() {
        return new Fail(subsetter);
    }
    
    public Fail fail(String reason) {
        return new Fail(subsetter, reason);
    }
    
    public Fail fail(String reason, Throwable exception) {
        return new Fail(subsetter, reason, exception);
    }
    
    Subsetter subsetter;
}

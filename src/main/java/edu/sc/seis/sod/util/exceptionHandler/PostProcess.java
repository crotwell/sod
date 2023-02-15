package edu.sc.seis.sod.util.exceptionHandler;


public interface PostProcess {
    
    public void process(String message, Throwable thrown);
    
}

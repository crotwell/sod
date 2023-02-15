package edu.sc.seis.sod.util.exceptionHandler;


/**
 * TestExceptionHandler.java
 *
 *
 * Created: Thu Jan 31 17:03:09 2002
 *
 * @author Srinivasa Telukutla
 * @version
 */

public class TestExceptionHandler {
    public TestExceptionHandler (){
    
    }
    public static void main(String args[]) {
    String num = "abcd";
    
    try {
        
        int no = Integer.parseInt(num);
    } catch(Exception e) {
        GlobalExceptionHandler.handle("Number Format Exception", e);
    }
    }
}// TestExceptionHandler

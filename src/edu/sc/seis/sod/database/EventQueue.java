package edu.sc.seis.sod.database;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.*;

import java.util.*;

/**
 * This is an implementation of the Queue DataStructure. 
 * This  class is thread safe.  
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class EventQueue {


    /**
     * constructor.
     *
     */
    public EventQueue() {
	super();
		
    }

    /**
     * inserts the obj at the end of the queue.
     * 
     * @param obj a <code>java.lang.Object</code> value to be inserted into the queue.
     */
    public synchronized void push(java.lang.Object obj) {
		
	while(list.size() == 24) {
	    try {
		System.out.println("Watiting in PUSH");
		wait();
	    } catch(InterruptedException ie) { }
	}
	list.add(0, obj);
	notifyAll();
    }

    /**
     * pops the first element of the queue.
     * @return a <code>java.lang.Object</code> value
     */
    public synchronized java.lang.Object pop() {
	
	while(list.size() == 0 && sourceAlive == true) {
	    try {
		System.out.println("Waiting in POP");
		wait();
	    } catch(InterruptedException ie) { }

	}
	if(list.size() == 0) return null;
	java.lang.Object obj = list.get(list.size()-1);
	list.remove(list.size() - 1);
	notifyAll();
	return obj;

    }

    /**
     * returns the length of the queue.
     *
     * @return an <code>int</code> value
     */
    public synchronized int getLength() {
	
	return list.size();
	
    }

    /**
     * sets if the source i.e., the thread which pushes objects into the queue
     * is alive 
     *
     * @param value a <code>boolean</code> value
     */
    public synchronized void setSourceAlive(boolean value) {

	this.sourceAlive = value;
	System.out.println("Settting the value of the source to "+value);
	notifyAll();
    }
    
    /**
     * returns true if the source i.e., the thread which pushes objects into the queue
     * is alive, else returns false.
     *
     * @return a <code>boolean</code> value
     */
    public synchronized boolean getSourceAlive() {
	return this.sourceAlive;
    }

    public void waitForProcessing() {

    }
	
    private boolean sourceAlive = true;

    private List list = new LinkedList();
	
}

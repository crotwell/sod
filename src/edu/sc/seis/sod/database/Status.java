package edu.sc.seis.sod.database;

/**
 * Status.java
 *
 *
 * Created: Wed Sep 18 12:13:20 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class Status {
    private  Status (int status){
	this.status = status;
    }

    public static Status getById(int status) {
	switch(status) {

	case 0:
	    return NEW;
	case 1:
	    return PROCESSING;
	case 2:
	    return COMPLETE_SUCCESS;
	case 3:
	    return COMPLETE_REJECT;
	default:
	    return COMPLETE_SUCCESS;
	}
    }

    public int getId() {
	return this.status;
    }
    
    public final static Status NEW = new Status(0);

    public final static Status PROCESSING = new Status(1);

    public final static Status COMPLETE_SUCCESS = new Status(2);

    public final static Status COMPLETE_REJECT = new Status(3);
    
    private int status;
}// Status

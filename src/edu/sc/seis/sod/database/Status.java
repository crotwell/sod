package edu.sc.seis.sod.database;

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
            case 4:
                return RE_OPEN;
            case 5:
                return RE_OPEN_PROCESSING;
            case 6:
                return RE_OPEN_SUCCESS;
            case 7:
                return RE_OPEN_REJECT;
            case 8:
                return AWAITING_FINAL_STATUS;
            case 9:
                return SOD_FAILURE;
            default:
                return COMPLETE_SUCCESS;
        }
    }
    
    public int getId() {
        return this.status;
    }
    
    public String toString() {
        switch(status) {
            case 0:
                return "NEW";
            case 1:
                return "PROCESSING";
            case 2:
                return "COMPLETE_SUCCESS";
            case 3:
                return "COMPLETE_REJECT";
            case 4:
                return "RE_OPEN";
            case 5:
                return "RE_OPEN_PROCESSING";
            case 6:
                return "RE_OPEN_SUCCESS";
            case 7:
                return "RE_OPEN_REJECT";
            case 8:
                return "AWAITING_FINAL_STATUS";
            case 9:
                return "SOD_FAILURE";
            default:
                return "COMPLETE_SUCCESS";
        }
        
    }
    
    public final static Status NEW = new Status(0);
    
    public final static Status PROCESSING = new Status(1);
    
    public final static Status COMPLETE_SUCCESS = new Status(2);
    
    public final static Status COMPLETE_REJECT = new Status(3);
    
    public final static Status RE_OPEN = new Status(4);
    
    public final static Status RE_OPEN_PROCESSING = new Status(5);
    
    public final static Status RE_OPEN_SUCCESS = new Status(6);
    
    public final  static Status RE_OPEN_REJECT = new Status(7);
    
    public final static Status AWAITING_FINAL_STATUS = new Status(8);
    
    public final static Status SOD_FAILURE = new Status(9);
    
    private int status;
}// Status

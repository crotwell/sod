package edu.sc.seis.sod;

public class Status{
    private Status(byte packedRepr){
        this.type = (byte)(packedRepr/16);
        this.stage = (byte)(packedRepr%16);
        this.packedRepr = packedRepr;
    }

    public String toString(){
        if(type == 0) return SPECIAL_STRINGS[stage];
        return STAGE_STRINGS[stage] + " " + TYPE_STRINGS[type];
    }

    /**
     * This returns the status as a byte.  The first bit is unused.  Bits 2-4
     * contains the type.   Bits 5-8 contain the stage.
     */
    public byte getAsByte(){ return packedRepr; }

    public int getType(){ return type; }

    public int getStage(){ return stage; }

    private byte type, stage, packedRepr;

    //These are the types stored in the 4 high order bits of each status'
    //packedRepr
    public static final byte SPECIAL = 0, IN_PROG = 1, REJECT = 2, RETRY = 3,
        CORBA_FAILURE = 4, SYSTEM_FAILURE = 5;

    public static final String[] TYPE_STRINGS = { "", "in progress", "rejected",
            "scheduled for retry", "had a corba failure", "had a system failure" };

    //These are the two SPECIAL type stages.  Specify SPECIAL for the type to
    //get to them
    public static final byte NEW = 0, SUCCESS = 1;

    public static final String[] SPECIAL_STRINGS = { "New", "Success" };

    //These are the stages stuffed in the 4 low order bits of each status'
    //packedRepr
    public static final byte EVENT_ATTR_SUBSETTER = 0, EVENT_ORIGIN_SUBSETTER = 1,
        NETWORK_SUBSETTER = 2, EVENT_STATION_SUBSETTER = 3,
        EVENT_CHANNEL_SUBSETTER = 4, REQUEST_SUBSETTER = 5,
        AVAILABLE_DATA_SUBSETTER = 6, DATA_SUBSETTER = 7, PROCESSOR = 8;

    private static final String[] STAGE_STRINGS = { "Event attribute subsetter",
            "Event origin subsetter", "Network subsetter", "Event station subsetter",
            "Event channel subsetter", "Request subsetter",
            "Available data subsetter", "Data subsetter", "Processor" };

    public static Status get(int stage, int type){
        if(type >= TYPE_STRINGS.length || stage >= STAGE_STRINGS.length){
            String msg = "You passed in a type byte of " + type +
                " but the allowable type range is from 0 to " +
                TYPE_STRINGS.length + " and the allowable stage range is 0 to "
                + STAGE_STRINGS.length;
            throw new IllegalArgumentException(msg);
        }
        if(type == 0 && stage >= SPECIAL_STRINGS.length){
            String msg = "You selected one of the special types with type value "
                + 0 + ", but the stage you selected "+ stage +
                " is outside of the allowable special stage range";
            throw new IllegalArgumentException(msg);
        }
        return all[type * 16 + stage];
    }

    public static Status get(int packedRepresentation){
        return all[packedRepresentation];
    }

    public static Status get(String nestedText) {
        for (int i = 0; i < all.length; i++) {
            if(all.toString().equals(nestedText)) return all[i];
        }
        throw new IllegalArgumentException("No such status for string " + nestedText);
    }

    private static Status[] all = new Status[127];

    //This is just a convenient way of creating all 127 possible status values
    //This will create extra invalid statuses in the unused space, but users
    //can't get to them because the get method checks for valid types and stages
    //If you're hell bent on ensuring type safety for these, feel free to add
    //enough variables to this class to represent the entire status matrix
    static{ for (byte i = 0; i < all.length; i++) all[i] = new Status(i); }
}


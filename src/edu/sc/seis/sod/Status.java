package edu.sc.seis.sod;

import edu.iris.Fissures.model.UnitImpl;

public class Status{
    private Status(byte packedRepr){
        this.type = (byte)(packedRepr/16);
        this.stage = (byte)(packedRepr%16);
        this.packedRepr = packedRepr;
    }

    public String toString(){
        if(type == SPECIAL) return SPECIAL_STRINGS[stage];
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

    public static final String[] TYPE_STRINGS = new String[6];

    static {
        TYPE_STRINGS[SPECIAL]        = "";
        TYPE_STRINGS[IN_PROG]        = "in progress";
        TYPE_STRINGS[REJECT]         = "rejected";
        TYPE_STRINGS[RETRY]          = "scheduled for retry";
        TYPE_STRINGS[CORBA_FAILURE]  = "had a corba failure";
        TYPE_STRINGS[SYSTEM_FAILURE] = "had a system failure";
    };

    //These are the two SPECIAL type stages.  Specify SPECIAL for the type to
    //get to them
    public static final byte NEW = 0, SUCCESS = 1;

    public static final String[] SPECIAL_STRINGS = { "New", "Success" };

    //These are the stages stuffed in the 4 low order bits of each status'
    //packedRepr
    public static final byte EVENT_ATTR_SUBSETTER = 0, EVENT_ORIGIN_SUBSETTER = 1,
        NETWORK_SUBSETTER = 2, EVENT_STATION_SUBSETTER = 3,
        EVENT_CHANNEL_SUBSETTER = 4, REQUEST_SUBSETTER = 5,
        AVAILABLE_DATA_SUBSETTER = 6, DATA_SUBSETTER = 7, PROCESSOR = 8, EVENT_CHANNEL_POPULATION = 9;

    private static final String[] STAGE_STRINGS = new String[10];

    static {
        STAGE_STRINGS[EVENT_ATTR_SUBSETTER]     = "Event attribute subsetter";
        STAGE_STRINGS[EVENT_ORIGIN_SUBSETTER]   = "Event origin subsetter";
        STAGE_STRINGS[NETWORK_SUBSETTER]        = "Network subsetter";
        STAGE_STRINGS[EVENT_STATION_SUBSETTER]  = "Event station subsetter";
        STAGE_STRINGS[EVENT_CHANNEL_SUBSETTER]  = "Event channel subsetter";
        STAGE_STRINGS[REQUEST_SUBSETTER]        = "Request subsetter";
        STAGE_STRINGS[AVAILABLE_DATA_SUBSETTER] = "Available data subsetter";
        STAGE_STRINGS[DATA_SUBSETTER]           = "Data subsetter";
        STAGE_STRINGS[PROCESSOR]                = "Processor" ;
        STAGE_STRINGS[EVENT_CHANNEL_POPULATION] = "Event Channel Population" ;
    }

    public static Status get(int stage, int type){
        if(type >= TYPE_STRINGS.length || stage >= STAGE_STRINGS.length){
            String msg = "You passed in a type byte of " + type +
                " and a stage byte of "+stage+
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

    public static int getFieldInt(String fieldName) {
        try {
            return Status.class.getField(fieldName.toUpperCase()).getInt(null);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("field "+fieldName+" cannot be found in Status");
        } catch (IllegalAccessException e) {
            // this should not happen since the
            //NoSuchFieldException wasn't thrown by class.getField()

            throw new RuntimeException("This was supposed to be unreachable");
        }
    }

    public static Status[] getAllForType(String typeFieldName) {
        int fieldInt = getFieldInt(typeFieldName);
        Status[] out;
        if (typeFieldName.equals("NEW") || typeFieldName.equals("SUCCESS")) {
            // should be only one
            out = new Status[1];
            out[0] = get(SPECIAL, fieldInt);
        } else {
            out = new Status[TYPE_STRINGS.length];
            for (int i = 0; i < out.length; i++) {
                out[i] = get(i, fieldInt);
            }
        }
        return out;
    }

    private static Status[] all = new Status[127];

    //This is just a convenient way of creating all 127 possible status values
    //This will create extra invalid statuses in the unused space, but users
    //can't get to them because the get method checks for valid types and stages
    //If you're hell bent on ensuring type safety for these, feel free to add
    //enough variables to this class to represent the entire status matrix
    static{ for (byte i = 0; i < all.length; i++) all[i] = new Status(i); }
}


/**
 * Stage.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.model.status;

import java.io.Serializable;

public class Stage implements Serializable {

    private Stage(int val, String name) {
        this.val = val;
        this.name = name;
    }

    static final public Stage EVENT_ATTR_SUBSETTER      = new Stage(0, "Event attribute subsetter");
    static final public Stage EVENT_ORIGIN_SUBSETTER    = new Stage(1, "Event origin subsetter");
    static final public Stage NETWORK_SUBSETTER         = new Stage(2, "Network subsetter");
    static final public Stage EVENT_STATION_SUBSETTER   = new Stage(3, "Event station subsetter");
    static final public Stage EVENT_CHANNEL_SUBSETTER   = new Stage(4, "Event channel subsetter");
    static final public Stage REQUEST_SUBSETTER         = new Stage(5, "Request subsetter");
    static final public Stage AVAILABLE_DATA_SUBSETTER  = new Stage(6, "Available data subsetter");
    static final public Stage DATA_RETRIEVAL            = new Stage(7, "Data retrieval");
    static final public Stage PROCESSOR                 = new Stage(8, "Processor" );
    static final public Stage EVENT_CHANNEL_POPULATION  = new Stage(9, "Event Channel Population");

    static final public Stage[] ALL = {
            EVENT_ATTR_SUBSETTER,
            EVENT_ORIGIN_SUBSETTER,
            NETWORK_SUBSETTER,
            EVENT_STATION_SUBSETTER,
            EVENT_CHANNEL_SUBSETTER,
            REQUEST_SUBSETTER,
            AVAILABLE_DATA_SUBSETTER,
            DATA_RETRIEVAL ,
            PROCESSOR,
            EVENT_CHANNEL_POPULATION
    };

    public String toString() {
        return name;
    }

    public int getVal() {
        return val;
    }

    public static Stage getFromInt(int val) {
        return ALL[val];
    }

    private int val;

    private String name;
}


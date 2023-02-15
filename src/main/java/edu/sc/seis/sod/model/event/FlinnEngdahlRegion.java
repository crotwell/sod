// **********************************************************************
//
// Generated by the ORBacus IDL to Java Translator
//
// Copyright (c) 2000
// Object Oriented Concepts, Inc.
// Billerica, MA, USA
//
// All Rights Reserved
//
// **********************************************************************

// Version: 4.0.5

package edu.sc.seis.sod.model.event;

import java.io.Serializable;

//
// IDL:iris.edu/Fissures/FlinnEngdahlRegion:1.0
//
/** Holds the FlinnEngdahl Region. The convention is that number = 0 means
 *that the region is unknown. */

public class FlinnEngdahlRegion implements Serializable
{
    //
    // IDL:iris.edu/Fissures/FlinnEngdahlRegion/type:1.0
    //
    /***/

    public FlinnEngdahlType type;

    //
    // IDL:iris.edu/Fissures/FlinnEngdahlRegion/number:1.0
    //
    /***/

    public int number;

    public FlinnEngdahlRegion(FlinnEngdahlType type,
                                  int number) {
        this.type = type;
        this.number = number;
    }

    protected  FlinnEngdahlRegion() {}

    public static Serializable createEmpty() {
        return new  FlinnEngdahlRegion();
    }

    /** for use by hibernate */
    public int getTypeAsInt() {
        return type.value();
    }

    /** for use by hibernate */
    protected void setTypeAsInt(int type) {
        this.type = FlinnEngdahlType.from_int(type);
    }
    
    public String toString() {
        if (type.equals(FlinnEngdahlType.GEOGRAPHIC_REGION)) {
            return "Geo FERegion "+number;
        } else {
            return "Seis FERegion "+number;
        }
    }

    public boolean equals(Object o){
        if(o == this){ return true; }
        else if(o instanceof FlinnEngdahlRegion){
            FlinnEngdahlRegion oRegion = (FlinnEngdahlRegion)o;
            if(oRegion.number == number &&
               type == oRegion.type){
                return true;
            }
        }
        return false;
    }

    public int hashCode(){
        if(type == FlinnEngdahlType.GEOGRAPHIC_REGION){ return number; }
        else{ return number * -1;}
    }

}

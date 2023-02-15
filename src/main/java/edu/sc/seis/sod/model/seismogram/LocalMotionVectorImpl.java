package edu.sc.seis.sod.model.seismogram;

import java.time.Duration;
import java.time.Instant;

import edu.sc.seis.sod.model.common.ParameterRef;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.station.ChannelId;

/**
 * LocalMotionVectorImpl.java
 *
 *
 * Created: Thu Dec  6 21:20:33 2001
 *
 * @author Philip Crotwell
 */

public class LocalMotionVectorImpl extends MotionVectorAttrImpl {
    
    public VectorComponent[] data;
    
    protected LocalMotionVectorImpl (){
	
    }

    public LocalMotionVectorImpl(String id,
				 Property[] properties,
				 Instant begin_time,
				 int num_points,
				 SamplingImpl sampling_info,
				 UnitImpl y_unit,
				 ChannelId[] channel_group,
				 ParameterRef[] parm_ids,
				 VectorComponent[] data) {
	super(id,
	      properties,
	      begin_time,
	      num_points,
	      sampling_info,
	      y_unit,
	      channel_group,
	      parm_ids);
	this.data = data;
    }

    /** A factory method to create an empty LocalDataSetImpl. 
     *  This is to be used only by the ORB for unmarshelling
     *  valuetypes that have been sent via IIOP.
     */
    public static java.io.Serializable createEmpty() {
        return new LocalMotionVectorImpl();
    }

    public String get_id() {
	return id;
    }

}// LocalMotionVectorImpl

package edu.sc.seis.sod.bag;

import edu.sc.seis.sod.model.seismogram.LocalMotionVectorImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.VectorComponent;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.NetworkIdUtil;

/**
 * MotionVectorUtil.java
 *
 *
 * Created: Sat Oct 19 11:29:38 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version
 */

public class MotionVectorUtil {

    /** Creates a motion vector from 3 seismograms.
     *	@throws IncompatibleSeismograms if the channel ids
     *  or the time basis of the seismograms are not compatible.
     */	
    public static LocalMotionVectorImpl create(LocalSeismogramImpl[] seismograms)
	throws IncompatibleSeismograms {
	for (int k=1; k<2; k++) {
	     
	    if ( NetworkIdUtil.areEqual(seismograms[0].channel_id.getNetworkId(),
					seismograms[k].channel_id.getNetworkId())) {
		throw new IncompatibleSeismograms("Networks for 0 and "+k+" are not the same, "
						  +seismograms[0].channel_id.getNetworkId()
						  +" "
						  +seismograms[k].channel_id.getNetworkId());
	    }
	    
	    if ( ! seismograms[0].channel_id.getStationCode().equals(
								 seismograms[k].channel_id.getStationCode())) {
		throw new IncompatibleSeismograms("Station codes for 0 and "+k+" are not the same. "
						  +seismograms[0].channel_id.getStationCode()
						  +" "
						  +seismograms[k].channel_id.getStationCode());
	    }
	    if ( ! seismograms[0].channel_id.getLocCode().equals(
							      seismograms[k].channel_id.getLocCode())) {
		throw new IncompatibleSeismograms("Site codes for 0 and "+k+" are not the same. "
						  +seismograms[0].channel_id.getLocCode()
						  +" "
						  +seismograms[k].channel_id.getLocCode());
	    }
	    if ( ! seismograms[0].channel_id.getLocCode().equals(
							      seismograms[k].channel_id.getLocCode())) {
		throw new IncompatibleSeismograms("Site codes for 0 and "+k+" are not the same. "
						  +seismograms[0].channel_id.getLocCode()
						  +" "
						  +seismograms[k].channel_id.getLocCode());
	    }
	    if ( ! seismograms[0].begin_time.equals(seismograms[k].begin_time)) {
		throw new IncompatibleSeismograms("Site codes for 0 and "+k+" are not the same. "
						  +seismograms[0].channel_id.getLocCode()
						  +" "
						  +seismograms[k].channel_id.getLocCode());
	    }
	    if ( seismograms[0].num_points != seismograms[k].num_points) {
		throw new IncompatibleSeismograms("Number of points for 0 and "+k+" are not the same. "
						  +seismograms[0].num_points
						  +" "
						  +seismograms[k].num_points);
	    } // end of if ()
	    
	} // end of for (int k=1; k<2; k++)
	


	// all checks pass, so put into a motion vector
	ChannelId[] channel_group = new ChannelId[3];
	VectorComponent[] data = new VectorComponent[3];
	channel_group[0] = seismograms[0].channel_id;
	data[0] = new VectorComponent(seismograms[0].channel_id, 
				      seismograms[0].data);
	channel_group[1] = seismograms[1].channel_id;
	data[1] = new VectorComponent(seismograms[1].channel_id, 
				      seismograms[1].data);
	channel_group[2] = seismograms[2].channel_id;
	data[1] = new VectorComponent(seismograms[2].channel_id, 
				      seismograms[2].data);
	return new  LocalMotionVectorImpl(seismograms[0].get_id()+"MotionVec",
					  seismograms[0].properties,
					  seismograms[0].begin_time,
					  seismograms[0].num_points,
					  seismograms[0].sampling_info,
					  seismograms[0].y_unit,
					  channel_group,
					  seismograms[0].parm_ids,
					  data);
    }
    
}// MotionVectorUtil

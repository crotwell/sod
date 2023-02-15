
package edu.sc.seis.sod.model.seismogram;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

import edu.sc.seis.sod.model.common.ParameterRef;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.common.UnsupportedFormat;
import edu.sc.seis.sod.model.station.ChannelId;

/**
 * Implementation of the SeismogramAttr abstract class that is generated from
 * the IDL compiler. This holds the metadata for a seismogram, ie everything
 * except the actual timeseries data points.
 *
 *
 * Created: Wed Feb 23 12:51:21 2000
 *
 * @author Philip Crotwell
 * @version
 */

public class SeismogramAttrImpl implements Serializable {


    protected String id;

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/SeismogramAttr/properties:1.0
    //
    /***/

    public Property[] properties;

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/SeismogramAttr/begin_time:1.0
    //
    /***/

    public Instant begin_time;

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/SeismogramAttr/num_points:1.0
    //
    /***/

    public int num_points;

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/SeismogramAttr/sampling_info:1.0
    //
    /***/

    public SamplingImpl sampling_info;

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/SeismogramAttr/y_unit:1.0
    //
    /***/

    public UnitImpl y_unit;

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/SeismogramAttr/channel_id:1.0
    //
    /***/

    public ChannelId channel_id;

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/SeismogramAttr/parm_ids:1.0
    //
    /***/

    public ParameterRef[] parm_ids;

    
    /** A protected constructor that creates an empty SeismogramAttrImpl
     *  instance. This is to be used only by the ORB for unmarshelling
     *  valuetypes that have been sent via IIOP.
     */
    protected SeismogramAttrImpl() {

    }

    /** A factory method to create an empty SeismogramAttrImpl.
     *  This is to be used only by the ORB for unmarshelling
     *  valuetypes that have been sent via IIOP.
     */
    public static Serializable createEmpty() {
        return new SeismogramAttrImpl();
    }

    public SeismogramAttrImpl(String id,
                              Instant begin_time,
                              int num_points,
                              SamplingImpl sample_info,
                              UnitImpl y_unit,
                              ChannelId channel_id) {
        this(id, new Property[0], begin_time, num_points, sample_info, y_unit, channel_id, new ParameterRef[0]);
    }
    
    /** creates a new SeismogramAttrImpl.
     *
     * @param id The internal id for the seismogram. This is for the internal
     * use of the persistant storage system and should be unique within that
     * system.
     *
     * @param properties Properties for the seismogram, perhaps including a
     *     "Name" property for labeling a display.
     *
     * @param begin_time that time of the first sample in the seismogram.
     *
     * @param num_points The number of sample points.
     *
     * @param sample_info The frequency of sampling.
     *
     * @param y_unit The amplitude units of the seismogram.
     *
     * @param channel_id The id of the channel that recorded the seismogram.
     *
     */
    public SeismogramAttrImpl(String id,
                              Property[] properties,
                              Instant begin_time,
                              int num_points,
                              SamplingImpl sample_info,
                              UnitImpl y_unit,
                              ChannelId channel_id,
                              ParameterRef[] parm_ids) {
        this.id = id;
        this.begin_time = begin_time;
        this.num_points = num_points;
        this.sampling_info = sample_info;
        this.y_unit = y_unit;
        this.channel_id = channel_id;
        setProperties(properties);
        setParameterRefs(parm_ids);
    }

    /** @return the id of this seismogram. Should be unique.
     */
    public String get_id() {
        return id;
    }

    /** @return the number of data points within the seismogram.
     */
    public int getNumPoints() {
        return num_points;
    }

    public void setProperties(Property[] props) {
        if (props==null) {
            throw new IllegalArgumentException("Cannot set null properties.");
        }
        this.properties = props;
    }

    public Property[] getProperties() {
        return properties;
    }

    public String getProperty(String name) {
    for (int i=0; i<properties.length; i++) {
        if (properties[i].name.equals(name)) {
        return properties[i].value;
        } // end of if (properties[i].name.equals(name))
    } // end of for (int i=0; i<properties.length; i++)
    return null;
    }


    public void setProperty(String name, String value) {

    for(int i = 0; i < properties.length; i++) {
        if(properties[i].name.equals(name)) {
            properties[i].value = value;
            return;
        }
    }
    Property[] props  = new Property[this.properties.length + 1];
    System.arraycopy(this.properties, 0, props, 0, this.properties.length);
    props[this.properties.length] = new Property(name, value);
    setProperties(props);
    }

    public void setParameterRefs(ParameterRef[] parm_ids) {
    this.parm_ids = parm_ids;
    }

    public ParameterRef[] getParameterRefs() {
    return parm_ids;
    }

    /**
       Gets the time of the first sample.

       @return the time of the first sample as a MicroSecondDate.

       @throws UnsupportedFormat if the time string is not recognized.
    */
    public Instant getBeginTime() throws UnsupportedFormat {
        return begin_time;
    }

    /**@return the amount of time that this seismogram covers.
     */
    public Duration getTimeInterval() {
        return getSampling().getPeriod().multipliedBy(num_points-1);
    }


    /**
       @return the time of the last sample of this seismogram.
    */
    public Instant getEndTime() {
    return getBeginTime().plus(getTimeInterval());
    }

    /**
    @return the name given to this seismogram.
    */
    public String getName() {
        if (properties == null) return get_id();
        for (int i=0; i<properties.length; i++) {
            if (properties[i].name.equals("Name")) {
                return properties[i].value;
            }
        }
        return get_id();
    }

    /** Sets a name, for human readablilty. */
    public void setName(String name) {
        int nameNum = -1;
        if (properties == null || properties.length == 0) {
            this.properties = new Property[1];
            nameNum = 0;
        } else {
            for (int i=0; i<properties.length; i++) {
                if (properties[i].name.equals("Name")) {
                    nameNum = i;
                    break;
                }
            }
            // must not be there yet
        if(nameNum == -1) {
                Property[] tmp = new Property[properties.length+1];
                System.arraycopy(properties, 0, tmp, 0, properties.length);
                nameNum = properties.length-1;
                properties = tmp;
         }
        }
        properties[nameNum] = new Property("Name", name);
    }

    /** @return the units of amplitude of the timeseries.
     */
    public UnitImpl getUnit() {
        //cast to edu.iris.Fissures.model.Unit
    return (UnitImpl)y_unit;
    }

    /** @return the id of the channel that the seismogram was recorded from.
     */
    public ChannelId getChannelID() {
    return channel_id;
    }

    /** @return the samapling information for the seismogram. Note that this
    may be different from the nominal sampling rate of the channel if a
    sampling ajusting time correction has been applied.
    */
    public SamplingImpl getSampling() {
        return sampling_info;
    }

} // SeismogramAttrImpl

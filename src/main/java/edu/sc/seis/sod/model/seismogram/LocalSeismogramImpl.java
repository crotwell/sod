
package edu.sc.seis.sod.model.seismogram;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import edu.iris.dmc.seedcodec.B1000Types;
import edu.iris.dmc.seedcodec.Codec;
import edu.iris.dmc.seedcodec.CodecException;
import edu.iris.dmc.seedcodec.DecompressedData;
import edu.iris.dmc.seedcodec.UnsupportedCompressionType;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.ParameterRef;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.common.UnitRangeImpl;
import edu.sc.seis.sod.model.station.ChannelId;

/**
 * Implementation of the LocalSeismogram abstract class the is generated from
 * the LocalSeismogram valuetype byte the IDL compiler. Extends
 * SeismogramAttrImpl which in turn extends LocalSeismogram.
 *
 *
 * Created: Wed Feb 23 13:19:30 2000
 *
 * @author Philip Crotwell
 * @version $Id: LocalSeismogramImpl.java 22216 2011-03-17 16:58:27Z crotwell $
 */

public class LocalSeismogramImpl extends SeismogramAttrImpl {
    // extends LocalSeismogram {
    // hack to avoid diamond of death, SeismogramAttrImpl extends
    // LocalSeismogram, which is what we need

    public TimeSeriesDataSel data;


    /** Empty constructor to be used only for valuetype marshaling. */
    protected LocalSeismogramImpl() {

    }

    /** A factory method to create an empty LocalSeismogramImpl.
     *  This is to be used only by the ORB for unmarshelling
     *  valuetypes that have been sent via IIOP.
     */
    public static Serializable createEmpty() {
        return new LocalSeismogramImpl();
    }


    /** creates a new LocalSeismogramImpl. If data is null, num_points
     * is set to be 0.
     *
     * @param id The internal id for the seismogram. This is for the internal
     * use of the persistant storage system and should be unique within that
     * system.
     *
     * @param begin_time that time of the first sample in the seismogram.
     *
     * @param num_points The number of sample points.
     *
     * @param y_unit The amplitude units of the seismogram.
     *
     * @param channel_id The id of the channel that recorded the seismogram.
     *
     * @param data The SeismogramData obect that contains the actual timeseries
     *     data.
     */
    public LocalSeismogramImpl(String id,
                               Instant begin_time,
                               int num_points,
                               SamplingImpl sampling,
                               UnitImpl y_unit,
                               ChannelId channel_id,
                               TimeSeriesDataSel data) {
        this(id,
             new Property[0],
             begin_time,
             num_points,
             sampling,
             y_unit,
             channel_id,
             new ParameterRef[0],
             data);
    }

    /** creates a new LocalSeismogramImpl. If data is null, num_points
     * is set to be 0.
     *
     * @param id The internal id for the seismogram. This is for the internal
     * use of the persistant storage system and should be unique within that
     * system.
     *
     * @param begin_time that time of the first sample in the seismogram.
     *
     * @param num_points The number of sample points.
     *
     * @param y_unit The amplitude units of the seismogram.
     *
     * @param channel_id The id of the channel that recorded the seismogram.
     *
     * @param data The SeismogramData obect that contains the actual timeseries
     *     data.
     */
    public LocalSeismogramImpl(String id,
                               Instant begin_time,
                               int num_points,
                               SamplingImpl sampling,
                               UnitImpl y_unit,
                               ChannelId channel_id,
                               int[] data) {
        this(id,
             new Property[0],
             begin_time,
             num_points,
             sampling,
             y_unit,
             channel_id,
             new ParameterRef[0],
             new TimeSeriesDataSel());
        this.data.int_values(data);
    }

    /** creates a new LocalSeismogramImpl. If data is null, num_points
     * is set to be 0.
     *
     * @param id The internal id for the seismogram. This is for the internal
     * use of the persistant storage system and should be unique within that
     * system.
     *
     * @param begin_time that time of the first sample in the seismogram.
     *
     * @param num_points The number of sample points.
     *
     * @param y_unit The amplitude units of the seismogram.
     *
     * @param channel_id The id of the channel that recorded the seismogram.
     *
     * @param data The SeismogramData obect that contains the actual timeseries
     *     data.
     */
    public LocalSeismogramImpl(String id,
                               Instant begin_time,
                               int num_points,
                               SamplingImpl sampling,
                               UnitImpl y_unit,
                               ChannelId channel_id,
                               float[] data) {
        this(id,
             new Property[0],
             begin_time,
             num_points,
             sampling,
             y_unit,
             channel_id,
             new ParameterRef[0],
             new TimeSeriesDataSel());
        this.data.flt_values(data);
    }

    /** creates a new LocalSeismogramImpl. If data is null, num_points
     * is set to be 0.
     *
     * @param id The internal id for the seismogram. This is for the internal
     * use of the persistant storage system and should be unique within that
     * system.
     *
     * @param begin_time that time of the first sample in the seismogram.
     *
     * @param num_points The number of sample points.
     *
     * @param y_unit The amplitude units of the seismogram.
     *
     * @param channel_id The id of the channel that recorded the seismogram.
     *
     * @param data The SeismogramData obect that contains the actual timeseries
     *     data.
     */
    public LocalSeismogramImpl(String id,
                               Instant begin_time,
                               int num_points,
                               SamplingImpl sampling,
                               UnitImpl y_unit,
                               ChannelId channel_id,
                               double[] data) {
        this(id,
             new Property[0],
             begin_time,
             num_points,
             sampling,
             y_unit,
             channel_id,
             new ParameterRef[0],
             new TimeSeriesDataSel());
        this.data.dbl_values(data);
    }

    /** creates a new LocalSeismogramImpl. If data is null, num_points
     * is set to be 0.
     *
     * @param id The internal id for the seismogram. This is for the internal
     * use of the persistant storage system and should be unique within that
     * system.
     *
     * @param begin_time that time of the first sample in the seismogram.
     *
     * @param num_points The number of sample points.
     *
     * @param y_unit The amplitude units of the seismogram.
     *
     * @param channel_id The id of the channel that recorded the seismogram.
     *
     * @param data The SeismogramData obect that contains the actual timeseries
     *     data.
     */
    public LocalSeismogramImpl(String id,
                               Instant begin_time,
                               int num_points,
                               SamplingImpl sampling,
                               UnitImpl y_unit,
                               ChannelId channel_id,
                               short[] data) {
        this(id,
             new Property[0],
             begin_time,
             num_points,
             sampling,
             y_unit,
             channel_id,
             new ParameterRef[0],
             new TimeSeriesDataSel());
        this.data.sht_values(data);
    }


    /** creates a new LocalSeismogramImpl. If data is null, num_points
     * is set to be 0.
     *
     * @param id The internal id for the seismogram. This is for the internal
     * use of the persistant storage system and should be unique within that
     * system.
     *
     * @param properties Properties for the seismogram, perhaps including a
     *  name for labeling a display, or other small extra information. Stored
     *  as name-value pairs.
     *
     * @param begin_time that time of the first sample in the seismogram.
     *
     * @param num_points The number of sample points.
     *
     * @param y_unit The amplitude units of the seismogram.
     *
     * @param channel_id The id of the channel that recorded the seismogram.
     *
     *
     * @param data The SeismogramData obect that contains the actual timeseries
     *     data.
     */
    public LocalSeismogramImpl(String id,
                               Property[] properties,
                               Instant begin_time,
                               int num_points,
                               SamplingImpl sampling,
                               UnitImpl y_unit,
                               ChannelId channel_id,
                               ParameterRef[] parm_ids,
                               TimeSeriesDataSel data) {
        super(id,
              properties,
              begin_time,
              num_points,
              sampling,
              y_unit,
              channel_id,
              parm_ids);
        if (data == null) {
            this.num_points = 0;
        }
        if (y_unit==null) {
            throw new IllegalArgumentException("y_unit must not be null");
        }
        this.data = data;
    }

    /** creates a new LocalSeismogramImpl. If data is null, num_points
     * is set to be 0.
     *
     * @param id The internal id for the seismogram. This is for the internal
     * use of the persistant storage system and should be unique within that
     * system.
     *
     * @param properties Properties for the seismogram, perhaps including a
     *  name for labeling a display, or other small extra information. Stored
     *  as name-value pairs.
     *
     * @param begin_time that time of the first sample in the seismogram.
     *
     * @param num_points The number of sample points.
     *
     * @param y_unit The amplitude units of the seismogram.
     *
     * @param channel_id The id of the channel that recorded the seismogram.
     *
     *
     * @param data The SeismogramData obect that contains the actual timeseries
     *     data.
     */
    public LocalSeismogramImpl(String id,
                               Property[] properties,
                               Instant begin_time,
                               int num_points,
                               SamplingImpl sampling,
                               UnitImpl y_unit,
                               ChannelId channel_id,
                               ParameterRef[] parm_ids,
                               short[] data) {
        this(id,
             properties,
             begin_time,
             data.length,
             sampling,
             y_unit,
             channel_id,
             parm_ids,
             new TimeSeriesDataSel());
        this.data.sht_values(data);
    }

    /** creates a new LocalSeismogramImpl. If data is null, num_points
     * is set to be 0.
     *
     * @param id The internal id for the seismogram. This is for the internal
     * use of the persistant storage system and should be unique within that
     * system.
     *
     * @param properties Properties for the seismogram, perhaps including a
     *  name for labeling a display, or other small extra information. Stored
     *  as name-value pairs.
     *
     * @param begin_time that time of the first sample in the seismogram.
     *
     * @param num_points The number of sample points.
     *
     * @param y_unit The amplitude units of the seismogram.
     *
     * @param channel_id The id of the channel that recorded the seismogram.
     *
     *
     * @param data The SeismogramData obect that contains the actual timeseries
     *     data.
     */
    public LocalSeismogramImpl(String id,
                               Property[] properties,
                               Instant begin_time,
                               int num_points,
                               SamplingImpl sampling,
                               UnitImpl y_unit,
                               ChannelId channel_id,
                               ParameterRef[] parm_ids,
                               int[] data) {
        this(id,
             properties,
             begin_time,
             data.length,
             sampling,
             y_unit,
             channel_id,
             parm_ids,
             new TimeSeriesDataSel());
        this.data.int_values(data);
    }

    /** creates a new LocalSeismogramImpl. If data is null, num_points
     * is set to be 0.
     *
     * @param id The internal id for the seismogram. This is for the internal
     * use of the persistant storage system and should be unique within that
     * system.
     *
     * @param properties Properties for the seismogram, perhaps including a
     *  name for labeling a display, or other small extra information. Stored
     *  as name-value pairs.
     *
     * @param begin_time that time of the first sample in the seismogram.
     *
     * @param num_points The number of sample points.
     *
     * @param y_unit The amplitude units of the seismogram.
     *
     * @param channel_id The id of the channel that recorded the seismogram.
     *
     *
     * @param data The SeismogramData obect that contains the actual timeseries
     *     data.
     */
    public LocalSeismogramImpl(String id,
                               Property[] properties,
                               Instant begin_time,
                               int num_points,
                               SamplingImpl sampling,
                               UnitImpl y_unit,
                               ChannelId channel_id,
                               ParameterRef[] parm_ids,
                               float[] data) {
        this(id,
             properties,
             begin_time,
             data.length,
             sampling,
             y_unit,
             channel_id,
             parm_ids,
             new TimeSeriesDataSel());
        this.data.flt_values(data);
    }

    /** creates a new LocalSeismogramImpl. If data is null, num_points
     * is set to be 0.
     *
     * @param id The internal id for the seismogram. This is for the internal
     * use of the persistant storage system and should be unique within that
     * system.
     *
     * @param properties Properties for the seismogram, perhaps including a
     *  name for labeling a display, or other small extra information. Stored
     *  as name-value pairs.
     *
     * @param begin_time that time of the first sample in the seismogram.
     *
     * @param num_points The number of sample points.
     *
     * @param y_unit The amplitude units of the seismogram.
     *
     * @param channel_id The id of the channel that recorded the seismogram.
     *
     * @param data The SeismogramData obect that contains the actual timeseries
     *     data.
     */
    public LocalSeismogramImpl(String id,
                               Property[] properties,
                               Instant begin_time,
                               int num_points,
                               SamplingImpl sampling,
                               UnitImpl y_unit,
                               ChannelId channel_id,
                               ParameterRef[] parm_ids,
                               double[] data) {
        this(id,
             properties,
             begin_time,
             data.length,
             sampling,
             y_unit,
             channel_id,
             parm_ids,
             new TimeSeriesDataSel());
        this.data.dbl_values(data);
    }

    /** Constructs a new LocalSeismogram from the given attributes and data.
     */
    public LocalSeismogramImpl(SeismogramAttrImpl attr, short[] data) {
        this(attr, new TimeSeriesDataSel());
        this.data.sht_values(data);
        this.num_points = data.length;
    }

    /** Constructs a new LocalSeismogram from the given attributes and data.
     */
    public LocalSeismogramImpl(SeismogramAttrImpl attr, int[] data) {
        this(attr, new TimeSeriesDataSel());
        this.data.int_values(data);
        this.num_points = data.length;
    }

    /** Constructs a new LocalSeismogram from the given attributes and data.
     */
    public LocalSeismogramImpl(SeismogramAttrImpl attr, float[] data) {
        this(attr, new TimeSeriesDataSel());
        this.data.flt_values(data);
        this.num_points = data.length;
    }

    /** Constructs a new LocalSeismogram from the given attributes and data.
     */
    public LocalSeismogramImpl(SeismogramAttrImpl attr, double[] data) {
        this(attr, new TimeSeriesDataSel());
        this.data.dbl_values(data);
        this.num_points = data.length;
    }

    /** Constructs a new LocalSeismogram from the given attributes and data.
     Care should be taken as there is no check to verify that the number
     of data points is correct.
     */
    public LocalSeismogramImpl(SeismogramAttrImpl attr, TimeSeriesDataSel data) {
        this(attr.get_id(),
             attr.properties,
             attr.begin_time,
             attr.num_points,
             attr.sampling_info,
             attr.y_unit,
             attr.channel_id,
             attr.parm_ids,
             data);
    }

    /** Sets the attributes of this seismogram, use with caution!!!
     */
    public void setAttributes(SeismogramAttrImpl attr) {
        this.begin_time = attr.begin_time;
        this.num_points = attr.num_points;
        this.sampling_info = attr.sampling_info;
        this.y_unit = attr.y_unit;
        this.channel_id = attr.channel_id;
        this.parm_ids = attr.parm_ids;
        setProperties(attr.properties);
    }

    /** Sets the timeseries data for this seismogram. A check is made to
     ensure that num_points stays consistant with SeismogramData.
     */
    public void setData(TimeSeriesDataSel data) {
        this.data = data;
        dataChanged();
    }

    /** Sets the timeseries data for this seismogram. A check is made to
     ensure that num_points stays consistant with SeismogramData.
     */
    public void setData(short[] data) {
        TimeSeriesDataSel d = new TimeSeriesDataSel();
        d.sht_values(data);
        setData(d);
    }

    /** Sets the timeseries data for this seismogram. A check is made to
     ensure that num_points stays consistant with SeismogramData.
     */
    public void setData(int[] data) {
        TimeSeriesDataSel d = new TimeSeriesDataSel();
        d.int_values(data);
        setData(d);
    }
    /** Sets the timeseries data for this seismogram. A check is made to
     ensure that num_points stays consistant with SeismogramData.
     */
    public void setData(float[] data) {
        TimeSeriesDataSel d = new TimeSeriesDataSel();
        d.flt_values(data);
        setData(d);
    }

    /** Sets the timeseries data for this seismogram. A check is made to
     ensure that num_points stays consistant with SeismogramData.
     */
    public void setData(double[] data) {
        TimeSeriesDataSel d = new TimeSeriesDataSel();
        d.dbl_values(data);
        setData(d);
    }

    /** Sets the timeseries data for this seismogram. A check is made to
     ensure that num_points stays consistant with SeismogramData.
     */
    public void setData(EncodedData[] data) {
        TimeSeriesDataSel d = new TimeSeriesDataSel();
        d.encoded_values(data);
        setData(d);
    }

    /** @return the number of data points within the seismogram.
     */
    public int getNumPoints() {
        return num_points;
    }

    /** @return the minimum value of a data point.
     */
    public QuantityImpl getMinValue() throws CodecException {
        return getMinValue(0, getNumPoints());
    }

    /** @return the minimum value of a data point starting at startIndex
     *  and stopping at endIndex-1. */
    public QuantityImpl getMinValue(int startIndex,
                                    int endIndex)
        throws CodecException {
        if (startIndex < 0) {
            throw new IllegalArgumentException("Can't calculate min value for start index < 0, "+
                                                   startIndex);
        }
        if (endIndex > getNumPoints()) {
            throw new IllegalArgumentException("Can't calculate min value for end index > numPoints, "+
                                                   endIndex+", numPoints = "+getNumPoints());
        }
        if (startIndex >= endIndex) {
            throw new IllegalArgumentException("Can't calculate min value for start index >= end index, "+
                                                   " start index = "+startIndex+
                                                   " endIndex = "+endIndex);
        }

        if (decomp == null) {
            decodeBits();
        }
        double min = Float.MAX_VALUE;
        if (decomp.getType() == B1000Types.INTEGER) {
            // integer data
            int[] iData = decomp.getAsInt();
            for (int i=startIndex; i<iData.length && i<endIndex; i++) {
                if (iData[i] < min) {
                    min = iData[i];
                }
            }
        } else if (decomp.getType() == B1000Types.SHORT) {
            short[] iData = decomp.getAsShort();
            for (int i=startIndex; i<iData.length && i<endIndex; i++) {
                if (iData[i] < min) {
                    min = iData[i];
                }
            }
        } else if (decomp.getType() == B1000Types.FLOAT) {
            float[] iData = decomp.getAsFloat();
            for (int i=startIndex; i<iData.length && i<endIndex; i++) {
                if (iData[i] < min) {
                    min = iData[i];
                }
            }
        } else if (decomp.getType() == B1000Types.DOUBLE) {
            double[] iData = decomp.getAsDouble();
            for (int i=startIndex; i<iData.length && i<endIndex; i++) {
                if (iData[i] < min) {
                    min = iData[i];
                }
            }
        }
        return new QuantityImpl(min,
                                getUnit());
    }

    /** @return the maximum value of a data point.
     */
    public QuantityImpl getMaxValue() throws CodecException {
        return getMaxValue(0, getNumPoints());
    }

    /** @return the maximum vlue of a data point starting at startIndex
     and stopping at endIndex-1;
     */
    public QuantityImpl getMaxValue(int startIndex,
                                    int endIndex)
        throws CodecException {

        if (startIndex < 0) {
            throw new IllegalArgumentException("Can't calculate max value for start index < 0, "+
                                                   startIndex);
        }
        if (endIndex > getNumPoints()) {
            throw new IllegalArgumentException("Can't calculate max value for end index > numPoints, "+
                                                   endIndex+", numPoints = "+getNumPoints());
        }
        if (startIndex >= endIndex) {
            throw new IllegalArgumentException("Can't calculate max value for start index >= end index, "+
                                                   " start index = "+startIndex+
                                                   " endIndex = "+endIndex);
        }

        if (decomp == null) {
            decodeBits();
        }
        double max = -1*Float.MAX_VALUE;
        if (decomp.getType() == B1000Types.INTEGER) {
            // integer data
            int[] iData = decomp.getAsInt();
            for (int i=startIndex; i<iData.length && i<endIndex; i++) {
                if (iData[i] > max) {
                    max = iData[i];
                }
            }
        } else if (decomp.getType() == B1000Types.SHORT) {
            short[] iData = decomp.getAsShort();
            for (int i=startIndex; i<iData.length && i<endIndex; i++) {
                if (iData[i] > max) {
                    max = iData[i];
                }
            }
        } else if (decomp.getType() == B1000Types.FLOAT) {
            float[] iData = decomp.getAsFloat();
            for (int i=startIndex; i<iData.length && i<endIndex; i++) {
                if (iData[i] > max) {
                    max = iData[i];
                }
            }
        } else if (decomp.getType() == B1000Types.DOUBLE) {
            double[] iData = decomp.getAsDouble();
            for (int i=startIndex; i<iData.length && i<endIndex; i++) {
                if (iData[i] > max) {
                    max = iData[i];
                }
            }
        }
        return new QuantityImpl(max,
                                getUnit());
    }
    /** @return the mean value of the data points.
     */
    public QuantityImpl getMeanValue() throws CodecException {
        return getMeanValue(0, getNumPoints());
    }

    /** @return the mean value of the data points starting at startIndex
     and stopping at endIndex-1;
     */
    public QuantityImpl getMeanValue(int startIndex,
                                     int endIndex)
        throws CodecException {

        if (startIndex < 0) {
            throw new IllegalArgumentException("Can't calculate mean value for start index < 0, "+
                                                   startIndex);
        }
        if (endIndex > getNumPoints()) {
            throw new IllegalArgumentException("Can't calculate mean value for end index > numPoints, "+
                                                   endIndex+", numPoints = "+getNumPoints());
        }
        if (startIndex >= endIndex) {
            throw new IllegalArgumentException("Can't calculate mean value for start index >= end index, "+
                                                   " start index = "+startIndex+
                                                   " endIndex = "+endIndex);
        }

        if (decomp == null) {
            decodeBits();
        }

        double mean = 0.0;
        if (decomp.getType() == B1000Types.INTEGER) {
            // integer data
            int[] iData = decomp.getAsInt();
            for (int i=startIndex; i<iData.length && i<endIndex; i++) {
                mean+= iData[i];
            }
        } else if (decomp.getType() == B1000Types.SHORT) {
            short[] iData = decomp.getAsShort();
            for (int i=startIndex; i<iData.length && i<endIndex; i++) {
                mean += iData[i];
            }
        } else if (decomp.getType() == B1000Types.FLOAT) {
            float[] iData = decomp.getAsFloat();
            for (int i=startIndex; i<iData.length && i<endIndex; i++) {
                mean += iData[i];
            }
        } else if (decomp.getType() == B1000Types.DOUBLE) {
            double[] iData = decomp.getAsDouble();
            for (int i=startIndex; i<iData.length && i<endIndex; i++) {
                mean+= iData[i];
            }
        }
        mean /= endIndex-startIndex;
        return new QuantityImpl(mean,
                                getUnit());
    }

    /** @return the value of the index data point.
     */
    public QuantityImpl getValueAt(int index)
        throws CodecException {
        if (decomp == null) {
            decodeBits();
        }
        if (decomp.getType() == B1000Types.SHORT) {
            return new QuantityImpl(decomp.getAsShort()[index], getUnit());
        } else if (decomp.getType() == B1000Types.INTEGER) {
            return new QuantityImpl(decomp.getAsInt()[index], getUnit());
        } else if (decomp.getType() == B1000Types.FLOAT) {
            return new QuantityImpl(decomp.getAsFloat()[index], getUnit());
        } else {
            return new QuantityImpl(decomp.getAsDouble()[index], getUnit());
        }
    }

    /** @return the amplitude range, min to max, from startIndex to
     endIndex-1
     */
    public UnitRangeImpl getAmplitudeRange(int beginIndex, int endIndex) throws CodecException {
        QuantityImpl min = getMinValue(beginIndex, endIndex);
        QuantityImpl max = getMaxValue(beginIndex, endIndex);
        return new UnitRangeImpl(min.getValue(),
                                 max.getValue(),
                                 min.getUnit());
    }

    /** @return the amplitude range for the whole seismogram.
     */
    public UnitRangeImpl getAmplitudeRange() throws CodecException {
        QuantityImpl min = getMinValue();
        QuantityImpl max = getMaxValue();
        return new UnitRangeImpl(min.getValue(),
                                 max.getValue(),
                                 min.getUnit());
    }

    /** @return true if the data is plain integers.
     */
    public boolean is_long() {
        TimeSeriesType discrim = data.discriminator();
        if (discrim.equals(TimeSeriesType.TYPE_LONG)) {
            return true;
        }
        return false;
    }

    /** @return true if the data is plain floats.
     */
    public boolean is_float() {
        TimeSeriesType discrim = data.discriminator();
        if (discrim.equals(TimeSeriesType.TYPE_FLOAT)) {
            return true;
        }
        return false;
    }

    /** @return true if the data is plain doubles.
     */
    public boolean is_double() {
        TimeSeriesType discrim = data.discriminator();
        if (discrim.equals(TimeSeriesType.TYPE_DOUBLE)) {
            return true;
        }
        return false;
    }

    /** @return true if the data is plain shorts.
     */
    public boolean is_short() {
        TimeSeriesType discrim = data.discriminator();
        if (discrim.equals(TimeSeriesType.TYPE_SHORT)) {
            return true;
        }
        return false;
    }

    /** @return true if the data is encoded.
     */
    public boolean is_encoded() {
        TimeSeriesType discrim = data.discriminator();
        if (discrim.equals(TimeSeriesType.TYPE_ENCODED)) {
            return true;
        }
        return false;
    }

    /** This should really be called can_convert_to_int, but corba calls a
     *  java int a long, and so the name is strange. */
    public boolean can_convert_to_long() {
        TimeSeriesType discrim = data.discriminator();
        if (discrim.equals(TimeSeriesType.TYPE_LONG) ||
            discrim.equals(TimeSeriesType.TYPE_SHORT)) {
            return true;
        } else if (discrim.equals(TimeSeriesType.TYPE_FLOAT) ||
                   discrim.equals(TimeSeriesType.TYPE_DOUBLE)) {
            return false;
        } else if (discrim.equals(TimeSeriesType.TYPE_ENCODED)) {

            // add support for more seed types

            // this is wrong as not all encodeddata objects in the array
            // must have the same compression format - fix later
            try {
                EncodedData[] ebb = data.encoded_values();
                if ( codec.getDecompressedType(ebb[0].compression) == B1000Types.INTEGER || codec.getDecompressedType(ebb[0].compression) == B1000Types.SHORT ) {
                    return true;
                } else {
                    return false;
                } // end of else
            } catch ( UnsupportedCompressionType e) {
                return false;
            } // end of try-catch

        } else {
            // can't happen
            // Unsupported Format???
            throw new RuntimeException("Can't happen!!! Unknown encoding format: "+
                                           discrim.value());
        }
    }

    /** @return true if the data is float, or encoded as floats.
     */
    public boolean can_convert_to_float() {
        TimeSeriesType discrim = data.discriminator();
        if (discrim.equals(TimeSeriesType.TYPE_DOUBLE)) {
            return false;
        } else if (discrim.equals(TimeSeriesType.TYPE_SHORT) ||
                   discrim.equals(TimeSeriesType.TYPE_LONG) ||
                   discrim.equals(TimeSeriesType.TYPE_FLOAT)) {
            return true;
        } else if (discrim.equals(TimeSeriesType.TYPE_ENCODED)) {
            EncodedData[] ebb = data.encoded_values();

            try {
                // this is wrong as not all encodeddata objects in the array
                // must have the same compression format - fix later
                if ( codec.getDecompressedType(ebb[0].compression) == B1000Types.INTEGER
                    || codec.getDecompressedType(ebb[0].compression) == B1000Types.SHORT
                    || codec.getDecompressedType(ebb[0].compression) == B1000Types.FLOAT) {
                    return true;
                } else {
                    return false;
                } // end of else
            } catch ( UnsupportedCompressionType e) {
                return false;
            } // end of catch

        } else {
            // can't happen
            throw new RuntimeException("Can't happen!!! Unknown encoding format: "+
                                           discrim.value());
        }
    }

    public boolean can_convert_to_double() {
        return true;
    }

    public boolean can_convert_to_short() {
        TimeSeriesType discrim = data.discriminator();
        if ( discrim.equals(TimeSeriesType.TYPE_SHORT)) {
            return true;
        } else if (discrim.equals(TimeSeriesType.TYPE_LONG) ||
                   discrim.equals(TimeSeriesType.TYPE_FLOAT) ||
                   discrim.equals(TimeSeriesType.TYPE_DOUBLE)) {
            return false;
        } else if (discrim.equals(TimeSeriesType.TYPE_ENCODED)) {

            // add support for more seed types

            // this is wrong as not all encodeddata objects in the array
            // must have the same compression format - fix later

            try {
                EncodedData[] ebb = data.encoded_values();
                // this is wrong as not all encodeddata objects in the array
                // must have the same compression format - fix later
                if ( codec.getDecompressedType(ebb[0].compression) == B1000Types.SHORT ) {
                    return true;
                } else {
                    return false;
                } // end of else
            } catch ( UnsupportedCompressionType e) {
                return false;
            } // end of catch

        } else {
            // can't happen
            // Unsupported Format???
            throw new RuntimeException("Can't happen!!! Unknown encoding format: "+
                                           discrim.value());
        }
    }

    /** @return the SeismogramDataImpl data.
     */
    public TimeSeriesDataSel getData() {
        return data;
    }

    protected FissuresException makeFissuresException(CodecException e) {
        FissuresException ee = makeFissuresException("Codec Exception: "+e.getMessage());
        ee.initCause(e);
        return ee;
    }

    protected FissuresException makeFissuresException(String e) {
        return new FissuresException(e);
    }

    public int[] get_as_longs() throws FissuresException {
        if (decomp == null) {
            try {
                decodeBits();
            } catch (CodecException e) {
                throw makeFissuresException(e);
            }
        }
        int[] out = decomp.getAsInt();
        if (out == null) {
            throw makeFissuresException("Can't convert to int "+decomp.getTypeString());
        }
        return out;
    }

    public short[] get_as_shorts() throws FissuresException {
        if (decomp == null) {
            try {
                decodeBits();
            } catch (CodecException e) {
                throw makeFissuresException(e);
            }
        }
        short[] out = decomp.getAsShort();
        if (out == null) {
            throw makeFissuresException("Can't convert to short "+decomp.getTypeString());
        }
        return out;
    }

    public float[] get_as_floats() throws FissuresException {
        if (decomp == null) {
            try {
                decodeBits();
            } catch (CodecException e) {
                throw makeFissuresException(e);
            }
        }
        float[] out = decomp.getAsFloat();
        if (out == null) {
            throw makeFissuresException("Can't convert to float "+decomp.getTypeString());
        }
        return out;
    }

    public double[] get_as_doubles() throws FissuresException {
        if (decomp == null) {
            try {
                decodeBits();
            } catch (CodecException e) {
                throw makeFissuresException(e);
            }
        }
        double[] out = decomp.getAsDouble();
        if (out == null) {
            throw makeFissuresException("Can't convert to double "+decomp.getTypeString());
        }
        return out;
    }

    public EncodedData[] get_as_encoded() throws FissuresException {
        if (is_encoded()) {
            return data.encoded_values();
        }
        throw makeFissuresException("data is not encoded");
    }

    /** Appends the newData to the existing data. Handles the case of null
     for existing data. The types of the data objects must be compatible.
     */
    public void append_longs(int[] newData) throws FissuresException {
        //        Logger.log("appending "+newData.length+" ints.");
        if (getData() == null) {
            TimeSeriesDataSel tsds = new TimeSeriesDataSel();
            tsds.int_values(newData);
            setData(tsds);
        } else {
            DataAppend.append(getData(), newData);
        }
        dataChanged();
    }

    /** Appends the newData to the existing data. Handles the case of null
     for existing data. The types of the data objects must be compatible.
     */
    public void append_shorts(short[] newData) throws FissuresException {
        if (getData() == null) {
            TimeSeriesDataSel tsds = new TimeSeriesDataSel();
            tsds.sht_values(newData);
            setData(tsds);
        } else {
            DataAppend.append(getData(), newData);
        }
        dataChanged();
    }

    /** Appends the newData to the existing data. Handles the case of null
     for existing data. The types of the data objects must be compatible.
     */
    public void append_floats(float[] newData) throws FissuresException {
        if (getData() == null) {
            TimeSeriesDataSel tsds = new TimeSeriesDataSel();
            tsds.flt_values(newData);
            setData(tsds);
        } else {
            DataAppend.append(getData(), newData);
        }
        dataChanged();
    }

    /** Appends the newData to the existing data. Handles the case of null
     for existing data. The types of the data objects must be compatible.
     */
    public void append_doubles(double[] newData) throws FissuresException {
        if (getData() == null) {
            TimeSeriesDataSel tsds = new TimeSeriesDataSel();
            tsds.dbl_values(newData);
            setData(tsds);
        } else {
            DataAppend.append(getData(), newData);
        }
        dataChanged();
    }

    /** Appends the newData to the existing data. Handles the case of null
     for existing data. The types of the data objects must be compatible.
     */
    public void append_encoded_seq(EncodedData[] newData) throws FissuresException {
        if (getData() == null) {
            TimeSeriesDataSel tsds = new TimeSeriesDataSel();
            tsds.encoded_values(newData);
            setData(tsds);
        } else {
            DataAppend.append(getData(), newData);
        }
        dataChanged();
    }

    /** Appends the newData to the existing data. Handles the case of null
     for existing data. The types of the data objects must be compatible.
     */
    public void append_encoded(EncodedData newData) throws FissuresException {
        EncodedData[] temp = new EncodedData[1];
        temp[0] = newData;
        append_encoded_seq(temp);
    }

    public int calculateNumPoints() {
        switch (data.discriminator().value()) {
            case TimeSeriesType._TYPE_SHORT:
                return data.sht_values().length;
            case TimeSeriesType._TYPE_LONG:
                return data.int_values().length;
            case TimeSeriesType._TYPE_FLOAT:
                return data.flt_values().length;
            case TimeSeriesType._TYPE_DOUBLE:
                return data.dbl_values().length;
            case TimeSeriesType._TYPE_ENCODED:
                int num = 0;
                EncodedData[] temp = data.encoded_values();
                for (int i=0; i<temp.length; i++) {
                    num += temp[i].num_points;
                }
                return num;
            default:
                // can't happen
                throw new RuntimeException("getNumPoints default");
        }
    }

    /** temporary */
    public float[] getValues(int start, int length) throws CodecException {
        if (decomp == null) {
            decodeBits();
        }
        float[] out = new float[length];
        System.arraycopy(decomp.getAsFloat(), start, out, 0, length);
        return out;
    }

    public TimeSeriesType getDataType() {
        return data.discriminator();
    }

    public void append(TimeSeriesDataSel b) throws FissuresException {
        switch (b.discriminator().value()) {
            case TimeSeriesType._TYPE_SHORT:
                append_shorts(b.sht_values());
                break;
            case TimeSeriesType._TYPE_LONG:
                append_longs(b.int_values());
                break;
            case TimeSeriesType._TYPE_FLOAT:
                append_floats(b.flt_values());
                break;
            case TimeSeriesType._TYPE_DOUBLE:
                append_doubles(b.dbl_values());
                break;
            case TimeSeriesType._TYPE_ENCODED:
                append_encoded_seq(b.encoded_values() );
                break;
            default:
                // can't happen
                throw new FissuresException("Unknown data type."+
                                                              b.discriminator().value() );
        }
        dataChanged();
    }

    public boolean isDataDecodable() {
        try {
            if (decomp == null) {
                decodeBits();
            }
            return true;
        } catch (CodecException e) {
            return false;
        }
    }

    public void dataChanged() {
        decomp = null;
        num_points = calculateNumPoints();
    }

    protected void decodeBits()
        throws CodecException {
        if (data.discriminator().equals(TimeSeriesType.TYPE_LONG)) {
            // straight ints
        	if (data.int_values().length != getNumPoints()) {
            	throw new CodecException("Number of points in data ("+data.int_values().length+
            			") is not the same as the number of points in the seismogram ("+getNumPoints()+").");
        	}
            decomp = new DecompressedData(data.int_values());
        } else if (data.discriminator().equals(TimeSeriesType.TYPE_SHORT)) {
            // straight shorts
        	if (data.sht_values().length != getNumPoints()) {
            	throw new CodecException("Number of points in data ("+data.sht_values().length+
            			") is not the same as the number of points in the seismogram ("+getNumPoints()+").");
        	}
            decomp = new DecompressedData(data.sht_values());
        } else if (data.discriminator().equals(TimeSeriesType.TYPE_FLOAT)) {
            // straight floats
        	if (data.flt_values().length != getNumPoints()) {
            	throw new CodecException("Number of points in data ("+data.flt_values().length+
            			") is not the same as the number of points in the seismogram ("+getNumPoints()+").");
        	}
            decomp = new DecompressedData(data.flt_values());
        } else if (data.discriminator().equals(TimeSeriesType.TYPE_DOUBLE)) {
            // straight doubles
        	if (data.dbl_values().length != getNumPoints()) {
            	throw new CodecException("Number of points in data ("+data.dbl_values().length+
            			") is not the same as the number of points in the seismogram ("+getNumPoints()+").");
        	}
            decomp = new DecompressedData(data.dbl_values());
        } else if (data.discriminator().equals(TimeSeriesType.TYPE_ENCODED)) {
            // encoded
            EncodedData[] eData = data.encoded_values();
        	// sanity check on num points
            int numFound = 0;
            for (int ii=0; ii<eData.length; ii++) {
            	numFound+=eData[ii].num_points;
            }
            if (numFound != getNumPoints()) {
            	throw new CodecException("Sum of number of points in all EncodedData objects ("+numFound+
            			") is not the same as the number of points in the seismogram ("+getNumPoints()+").");
            }
            List<DecompressedData> decompBuffer =
                new ArrayList<DecompressedData>(eData.length);

            // do we have to byte swap. ALL java is big endian, ie sparc, m68k
            // byte order regardless of the platform, so we only have
            // to byte swap if the file is little endian, x86 order

            //            Logger.log("getNumPoints()="+getNumPoints()+
            //         "  num encoded="+eData.length+
            //         "  compression="+eData[0].compression+
            //         "  byte order="+eData[0].byte_order);
            //for (int i=0; i<eData.length; i++) {
            //  Logger.log("eData["+i+"].npts="+eData[i].num_points);
            //  }
            for (int i=0; i<eData.length; i++) {
                if (eData[i].num_points != 0) {
                    // skip records with no data as they often have compression set to 0
                    decompBuffer.add(codec.decompress(eData[i].compression,
                                                      eData[i].values,
                                                      eData[i].num_points,
                                                      eData[i].byte_order));
                }
            }

            // search for least common demoninator of decompressed primitive
            // types. short =1, int = 3, float = 4, double = 5, so order is
            // numerical
            int type = -1;
            for (DecompressedData decompressedData : decompBuffer) {
                if ( decompressedData.getType() > type) {
                    type = decompressedData.getType();
                } // end of if ()
            }

            int numSoFar = 0;
            switch ( type ) {
                case B1000Types.SHORT:
                    short[] buffer = new short[getNumPoints()];
                    for (DecompressedData decompressedData : decompBuffer) {
                        short[] temp = decompressedData.getAsShort();
                        System.arraycopy(temp, 0,
                                         buffer, numSoFar,
                                         temp.length);
                        numSoFar += temp.length;
                    }
                    decomp = new DecompressedData(buffer);
                    break;
                case B1000Types.INTEGER:
                    int[] ibuffer = new int[getNumPoints()];
                    for (DecompressedData decompressedData : decompBuffer) {
                        int[] temp = decompressedData.getAsInt();
                        System.arraycopy(temp, 0,
                                         ibuffer, numSoFar,
                                         temp.length);
                        numSoFar += temp.length;
                    }
                    decomp = new DecompressedData(ibuffer);
                    break;
                case B1000Types.FLOAT:
                    float[] fbuffer = new float[getNumPoints()];
                    for (DecompressedData decompressedData : decompBuffer) {
                        float[] temp = decompressedData.getAsFloat();
                        System.arraycopy(temp, 0,
                                         fbuffer, numSoFar,
                                         temp.length);
                        numSoFar += temp.length;
                    }
                    decomp = new DecompressedData(fbuffer);
                    break;
                case B1000Types.DOUBLE:
                    double[] dbuffer = new double[getNumPoints()];
                    for (DecompressedData decompressedData : decompBuffer) {
                        double[] temp = decompressedData.getAsDouble();
                        System.arraycopy(temp, 0,
                                         dbuffer, numSoFar,
                                         temp.length);
                        numSoFar += temp.length;
                    }
                    decomp = new DecompressedData(dbuffer);
                    break;

                default:
                    throw new CodecException("decompressed primitive type "+type+
                                           " is not known");
            } // end of switch ()
        }
    }

    /** holds a decompressed copy of the data elements. This makes it possible
     *  to only have to uncompress compressed data once and cache it. The
     *  outside caller need not know, except that the first call to access
     *  data will take a little longer.*/
    protected transient DecompressedData decomp = null;

    /** decompression class that understands different compression routines.
     */
    protected transient static Codec codec = new Codec();


} // LocalSeismogramImpl


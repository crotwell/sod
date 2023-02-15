package edu.sc.seis.sod.util.convert.mseed;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.iris.dmc.seedcodec.B1000Types;
import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.mseed.Blockette;
import edu.sc.seis.seisFile.mseed.Blockette100;
import edu.sc.seis.seisFile.mseed.Blockette1000;
import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.BtimeRange;
import edu.sc.seis.seisFile.mseed.DataHeader;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.ParameterRef;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.common.UnitBase;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.seismogram.EncodedData;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.Plottable;
import edu.sc.seis.sod.model.seismogram.PlottableChunk;
import edu.sc.seis.sod.model.seismogram.Property;
import edu.sc.seis.sod.model.seismogram.TimeSeriesDataSel;
import edu.sc.seis.sod.model.seismogram.TimeSeriesType;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.util.display.IntRange;
import edu.sc.seis.sod.util.display.SimplePlotUtil;
import edu.sc.seis.sod.util.time.RangeTool;

/**
 * FissuresConvert.java
 * 
 * 
 * Created: Fri Oct 15 09:09:32 1999
 * 
 * @author Philip Crotwell
 * @version
 */
public class FissuresConvert {

    private FissuresConvert() {}

    public static DataRecord[] toMSeed(LocalSeismogramImpl seis) throws SeedFormatException {
        return toMSeed(seis, 1);
    }

    public static DataRecord[] toMSeed(LocalSeismogramImpl seis, int seqStart) throws SeedFormatException {
        LinkedList<DataRecord> outRecords = new LinkedList<DataRecord>();
        Instant start = seis.begin_time;
        if (seis.data.discriminator().equals(TimeSeriesType.TYPE_ENCODED)) {
            // encoded data
            EncodedData[] eData = seis.data.encoded_values();
            outRecords = toMSeed(eData, seis.channel_id, start, (SamplingImpl)seis.sampling_info, seqStart);
        } else if (seis.data.discriminator().equals(TimeSeriesType.TYPE_LONG)) {
            try {
                outRecords = toMSeed(toEncodedData(seis.get_as_longs()),
                                     seis.channel_id, start, (SamplingImpl)seis.sampling_info, seqStart);
            } catch(FissuresException e) {
                // this shouldn't ever happen as we already checked the type
                throw new SeedFormatException("Problem getting integer data", e);
            }
        } else if (seis.data.discriminator().equals(TimeSeriesType.TYPE_FLOAT)) {
            try {
                // for float, 64 bytes = 4 bytes * 16 samples, so each edata
                // holds 62*16 samples
                EncodedData[] eData = new EncodedData[(int)Math.ceil(seis.num_points * 4.0f / (62 * 64))];
                float[] data = seis.get_as_floats();
                for (int i = 0; i < eData.length; i++) {
                    byte[] dataBytes = new byte[62 * 64];
                    int j;
                    for (j = 0; j + (62 * 16 * i) < data.length && j < 62 * 16; j++) {
                        int val = Float.floatToIntBits(data[j + (62 * 16 * i)]);
                        dataBytes[4 * j] = (byte)((val & 0xff000000) >> 24);
                        dataBytes[4 * j + 1] = (byte)((val & 0x00ff0000) >> 16);
                        dataBytes[4 * j + 2] = (byte)((val & 0x0000ff00) >> 8);
                        dataBytes[4 * j + 3] = (byte)((val & 0x000000ff));
                    }
                    if (j == 0) {
                        throw new SeedFormatException("try to put 0 float samples into an encodedData object j=" + j
                                + " i=" + i + " seis.num_ppoints=" + seis.num_points);
                    }
                    eData[i] = new EncodedData((short)B1000Types.FLOAT, dataBytes, j, false);
                }
                outRecords = toMSeed(eData, seis.channel_id, start, (SamplingImpl)seis.sampling_info, seqStart);
            } catch(FissuresException e) {
                // this shouldn't ever happen as we already checked the type
                throw new SeedFormatException("Problem getting float data", e);
            }
        } else if (seis.data.discriminator().equals(TimeSeriesType.TYPE_LONG)) {
            try {
                // for int, 64 bytes = 4 bytes * 16 samples, so each edata
                // holds 62*16 samples
                EncodedData[] eData = new EncodedData[(int)Math.ceil(seis.num_points * 4.0f / (62 * 64))];
                int[] data = seis.get_as_longs();
                for (int i = 0; i < eData.length; i++) {
                    byte[] dataBytes = new byte[62 * 64];
                    int j;
                    for (j = 0; j + (62 * 16 * i) < data.length && j < 62 * 16; j++) {
                        int val = data[j + (62 * 16 * i)];
                        dataBytes[4 * j] = (byte)((val & 0xff000000) >> 24);
                        dataBytes[4 * j + 1] = (byte)((val & 0x00ff0000) >> 16);
                        dataBytes[4 * j + 2] = (byte)((val & 0x0000ff00) >> 8);
                        dataBytes[4 * j + 3] = (byte)((val & 0x000000ff));
                    }
                    if (j == 0) {
                        throw new SeedFormatException("try to put 0 int samples into an encodedData object j=" + j
                                + " i=" + i + " seis.num_ppoints=" + seis.num_points);
                    }
                    eData[i] = new EncodedData((short)B1000Types.INTEGER, dataBytes, j, false);
                }
                outRecords = toMSeed(eData, seis.channel_id, start, (SamplingImpl)seis.sampling_info, seqStart);
            } catch(FissuresException e) {
                // this shouldn't ever happen as we already checked the type
                throw new SeedFormatException("Problem getting float data", e);
            }
        } else {
            // not encoded
            throw new SeedFormatException("Can only handle EncodedData now, type=" + seis.data.discriminator().value());
            // int samples = seis.num_points;
            // while ( samples > 0 ) {
            // DataHeader header = new DataHeader(seqStart++, 'D', false);
            // ChannelId chan = seis.channel_id;
            // header.setStationIdentifier(chan.station_code);
            // header.setLocationIdentifier(chan.site_code);
            // header.setChannelIdentifier(chan.channel_code);
            // header.setNetworkCode(chan.network_id.network_code);
            // header.setStartTime(start);
            //
            // Blockette1000 b1000 = new Blockette1000();
            //
            // // b1000.setEncodeingFormat((byte)seis.);
            // DataRecord dr = new DataRecord(header);
            // } // end of while ()
        }
        return outRecords.toArray(new DataRecord[0]);
    }

    /*
    public static DataRecord[] toMSeed(DataChunk chunk) throws SeedFormatException {
        LinkedList<DataRecord> outRecords;
        if (chunk.data.discriminator().equals(TimeSeriesType.TYPE_ENCODED)) {
            outRecords = toMSeed(chunk.data.encoded_values(),
                                 chunk.channel,
                                 new MicroSecondDate(chunk.begin_time),
                                 DataCenterUtil.getSampling(chunk),
                                 chunk.seq_num);
        } else {
            throw new SeedFormatException("Can only handle EncodedData now");
        }
        return outRecords.toArray(new DataRecord[0]);
    }
    */

    public static LinkedList<DataRecord> toMSeed(EncodedData[] eData,
                                                 ChannelId channel_id,
                                                 Instant start,
                                                 SamplingImpl sampling_info,
                                                 int seqStart) throws SeedFormatException {
        return toMSeed(eData, channel_id, start, sampling_info, seqStart, 'M');
    }

    public static LinkedList<DataRecord> toMSeed(EncodedData[] eData,
                                                 ChannelId channel_id,
                                                 Instant start,
                                                 SamplingImpl sampling_info,
                                                 int seqStart,
                                                 char typeCode) throws SeedFormatException {
        LinkedList<DataRecord> list = new LinkedList<DataRecord>();
        DataHeader header;
        Blockette1000 b1000;
        Blockette100 b100;
        int recordSize = RECORD_SIZE_4096;
        int recordSizePower = RECORD_SIZE_4096_POWER;
        int minRecordSize = 0;   
        for (int i = 0; i < eData.length; i++) {
            header = new DataHeader(seqStart++, typeCode, false);
            b1000 = new Blockette1000();
            b100 = new Blockette100();
            if ( minRecordSize < eData[i].values.length + header.getSize() + b1000.getSize()) {
                minRecordSize = eData[i].values.length + header.getSize() + b1000.getSize();
            }
        }
        if (minRecordSize < RECORD_SIZE_4096) {
            recordSize = RECORD_SIZE_4096;
            recordSizePower = RECORD_SIZE_4096_POWER;
        }
        if (minRecordSize < RECORD_SIZE_1024) {
            recordSize = RECORD_SIZE_1024;
            recordSizePower = RECORD_SIZE_1024_POWER;
        }
        if (minRecordSize < RECORD_SIZE_512) {
            recordSize = RECORD_SIZE_512;
            recordSizePower = RECORD_SIZE_512_POWER;
        }
        for (int i = 0; i < eData.length; i++) {
            header = new DataHeader(seqStart++, 'D', false);
            b1000 = new Blockette1000();
            b100 = new Blockette100();
            if (eData[i].values.length + header.getSize() + b1000.getSize() + b100.getSize() < recordSize) {
                // ok to use Blockette100 for sampling
            } else if (eData[i].values.length + header.getSize() + b1000.getSize() < recordSize) {
                // will fit without Blockette100
                b100 = null;
            } else {
                throw new SeedFormatException("Can't fit data into record of size "+recordSize+" "+
                        + (eData[i].values.length + header.getSize() + b1000.getSize() + b100.getSize()) + " "
                        + eData[i].values.length + " " + (header.getSize() + b1000.getSize() + b100.getSize()));
            } // end of else
              // can fit into one record
            header.setStationIdentifier(channel_id.getStationCode());
            header.setLocationIdentifier(channel_id.getLocCode());
            header.setChannelIdentifier(channel_id.getChannelCode());
            header.setNetworkCode(channel_id.getNetworkId());
            header.setStartBtime(getBtime(start));
            header.setNumSamples((short)eData[i].num_points);
            Duration sampPeriod = sampling_info.getPeriod();
            start = start.plus(sampPeriod.multipliedBy(eData[i].num_points));
            short[] multiAndFactor = calcSeedMultipilerFactor(sampling_info);
            header.setSampleRateFactor(multiAndFactor[0]);
            header.setSampleRateMultiplier(multiAndFactor[1]);
            b1000.setEncodingFormat((byte)eData[i].compression);
            if (eData[i].byte_order) {
                // seed uses oposite convention
                b1000.setWordOrder((byte)0);
            } else {
                b1000.setWordOrder((byte)1);
            } // end of else
            b1000.setDataRecordLength((byte)recordSizePower);
            DataRecord dr = new DataRecord(header);
            dr.addBlockette(b1000);
            QuantityImpl hertz = sampling_info.getFrequency().convertTo(UnitImpl.HERTZ);
            if (b100 != null) {
                b100.setActualSampleRate((float)hertz.getValue());
                dr.addBlockette(b100);
            }
            dr.setData(eData[i].values);
            list.add(dr);
        } // end of for ()
        return list;
    }

    /** calculates the seed representation of a sample rate as factor and multiplier. */
    public static short[] calcSeedMultipilerFactor(SamplingImpl sampling) {
        double sps = sampling.getFrequency().getValue(UnitImpl.HERTZ);
        return DataHeader.calcSeedMultipilerFactor(sps);
    }

    public static LocalSeismogramImpl toFissures(String filename) throws SeedFormatException, IOException,
            FissuresException {
        List<DataRecord> data = new ArrayList<DataRecord>();
        DataInput dis = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
        try {
            while (true) {
                SeedRecord sr = SeedRecord.read(dis, 4096);
                if (sr instanceof DataRecord) {
                    data.add((DataRecord)sr);
                }
            }
        } catch(EOFException e) {}
        return toFissures(data.toArray(new DataRecord[0]));
    }

    /*

                                    if (seis == null) {
                                    logger.debug("Found beginning, records skipped="+numBeforeStart);
                                    seis = FissuresConvert.toFissures(dr);
                                    out.add(seis);
                                } else {
                                    TimeInterval fivePercent = (TimeInterval)seis.getSampling()
                                            .getPeriod()
                                            .multiplyBy(0.05);
                                    TimeInterval gap = seis.getEndTime()
                                            .add(seis.getSampling().getPeriod())
                                            .difference(drStart);
                                    if (gap.lessThanEqual(fivePercent)) {
                                        FissuresConvert.append(seis, dr);
                                    } else {
                                        logger.debug("create new due to gap > 5% samp:  "+gap+" > "+fivePercent);
                                        seis = FissuresConvert.toFissures(dr);
                                        out.add(seis);
                                    }
                                }
     */


    /**
     * assume all records from same channel and in time order. Separate seismograms created if there are
     * gaps/overlaps.
     */
    public static List<LocalSeismogramImpl> toFissures(List<DataRecord> seed) throws SeedFormatException, FissuresException {
        List<LocalSeismogramImpl> out = new ArrayList<LocalSeismogramImpl>();
        List<DataRecord> contiguous = new ArrayList<DataRecord>();
        DataRecord prev = null;
        for (DataRecord dr : seed) {
            if (prev != null && ! RangeTool.areContiguous(FissuresConvert.getTimeRange(prev.getBtimeRange()), 
                                                          FissuresConvert.getTimeRange(dr.getBtimeRange()),
                                                          FissuresConvert.convertSampleRate(prev).getPeriod())) {
                // probably should also check for chan match
                LocalSeismogramImpl seis = FissuresConvert.toFissuresSeismogram(contiguous);
                out.add(seis);
                contiguous.clear();
            }
            contiguous.add(dr);
            prev = dr;
        }
        if (contiguous.size() != 0) {
            LocalSeismogramImpl seis = FissuresConvert.toFissuresSeismogram(contiguous);
            out.add(seis);
        }
        return out;
    }
   
    /**
     * assume all records from same channel and in time order with no
     * gaps/overlaps.
     */
    public static LocalSeismogramImpl toFissuresSeismogram(List<DataRecord> seed) throws SeedFormatException, FissuresException {
        LocalSeismogramImpl seis = null;
        for (DataRecord dataRecord : seed) {
            if (seis == null) {
                seis = toFissures(dataRecord);
            } else {
                append(seis, dataRecord);
            }
        }
        return seis;
    }
    
    /**
     * assume all records from same channel and in time order with no
     * gaps/overlaps.
     */
    public static LocalSeismogramImpl toFissures(DataRecord[] seed) throws SeedFormatException, FissuresException {
        LocalSeismogramImpl seis = toFissures(seed[0]);
        // System.out.println("AFTER FIRST TO FISSURES: " +
        // seis.getBeginTime());
        for (int i = 1; i < seed.length; i++) {
            append(seis, seed[i]);
        }
        // System.out.println("AFTER APPEND: " + seis.getBeginTime());
        return seis;
    }

    /**
     * assume all records from same channel and in time order with no
     * gaps/overlaps. Specifying a default compression and byte order. This
     * should only be used in cases where the miniseed records are older than
     * the Blockette 1000 SEED specification and where the compression and byte
     * order are known from outside sources. Per the SEED specification, valid
     * miniseed MUST have a blockette 1000 and so this method exists only for
     * reading older data.
     * 
     * @param defaultCompression
     *            compression to use if there is no blockette 1000, See the SEED
     *            specification for blockette 1000 for valid compression types.
     * @param defaultByteOrder
     *            byte order to use if there is no blockette 1000. 0 indicates
     *            little-endian order and a 1 indicates big-endian.
     */
    public static LocalSeismogramImpl toFissures(DataRecord[] seed, byte defaultCompression, byte defaultByteOrder)
            throws SeedFormatException, FissuresException {
        DataRecord[] seedCopy = new DataRecord[seed.length];
        for (int i = 0; i < seed.length; i++) {
            if (seed[i].getBlockettes(1000).length == 0) {
                seedCopy[i] = new DataRecord(seed[i]);
                Blockette1000 fakeB1000 = new Blockette1000();
                fakeB1000.setEncodingFormat(defaultCompression);
                fakeB1000.setWordOrder(defaultByteOrder);
                fakeB1000.setDataRecordLength((byte)30);// should be huge and we
                                                        // will never write this
                                                        // out
                seedCopy[i].setRecordSize(seed[i].getRecordSize()*2); // make this bug enough for the
                                                 // extra blockette
                seedCopy[i].addBlockette(fakeB1000);
            } else {
                seedCopy[i] = seed[i];
            }
        }
        return toFissures(seedCopy);
    }

    /**
     * assume all records from same channel and in time order with no
     * gaps/overlaps.
     */
    public static LocalSeismogramImpl append(LocalSeismogramImpl seis, DataRecord[] seed) throws SeedFormatException,
            FissuresException {
        for (int i = 0; i < seed.length; i++) {
            append(seis, seed[i]);
        }
        return seis;
    }

    /**
     * assume all records from same channel and in time order with no
     * gaps/overlaps and small sampling rate variations. 
     * Sampling for seis is recalculated based on seis start time
     * and last sample time of DataRecord to average any sample rate variation
     */
    public static LocalSeismogramImpl append(LocalSeismogramImpl seis, DataRecord seed) throws SeedFormatException,
            FissuresException {
        TimeSeriesDataSel bits = convertData(seed);
        EncodedData[] edata = bits.encoded_values();
        for (int j = 0; j < edata.length; j++) {
            if (edata[j] == null) {
                throw new RuntimeException("encoded data is null " + j);
            }
            seis.append_encoded(edata[j]);
            Instant drEnd = seed.getLastSampleBtime().toInstant();
            seis.sampling_info = new SamplingImpl(seis.getNumPoints()-1, Duration.between(seis.getBeginTime(), drEnd));
        }
        return seis;
    }

    public static LocalSeismogramImpl toFissures(DataRecord seed) throws SeedFormatException {
        DataHeader header = seed.getHeader();
        String isoTime = getISOTime(header.getStartBtime());
        // the network id isn't correct, but network start is not stored
        // in miniseed
        Instant time = TimeUtils.parseISOString(isoTime);
        String netId = header.getNetworkCode();
        if (NetworkIdUtil.isTemporary(netId)) {
            netId = NetworkIdUtil.formId(netId, time);
        }
        ChannelId channelId = new ChannelId(netId,
                                            header.getStationIdentifier().trim(),
                                            edu.sc.seis.seisFile.fdsnws.stationxml.Channel.fixLocCode(header.getLocationIdentifier()), 
                                            header.getChannelIdentifier().trim(),
                                            time);
        String seisId = channelId.getNetworkId() + ":" + channelId.getStationCode() + ":" + channelId.getLocCode()
                + ":" + channelId.getChannelCode() + ":" + getISOTime(header.getStartBtime());
        Property[] props = new Property[1];
        props[0] = new Property("Name", seisId);
        SamplingImpl sampling = convertSampleRate(seed);
        TimeSeriesDataSel bits = convertData(seed);
        return new LocalSeismogramImpl(seisId,
                                       props,
                                       time,
                                       header.getNumSamples(),
                                       sampling,
                                       UnitImpl.COUNT,
                                       channelId,
                                       new ParameterRef[0],
                                       bits);
    }
    
    public static List<DataRecord> toMSeed(List<PlottableChunk> chunkList) throws SeedFormatException {
        List<DataRecord> out = new ArrayList<DataRecord>();
        int seqStart = 0;
        for (PlottableChunk chunk : chunkList) {
            ChannelId chan = new ChannelId(NetworkIdUtil.formId(chunk.getNetworkCode(), chunk.getBeginTime()),
                                           chunk.getStationCode(),
                                           chunk.getSiteCode(),
                                           chunk.getChannelCode(),
                                           chunk.getBeginTime());
            SamplingImpl samp = new SamplingImpl(chunk.getPixelsPerDay()*2, DAY);
            List<DataRecord> drList = toMSeed(toEncodedData(chunk.getYData()),
                                                            chan,
                                                            chunk.getBeginTime().plus(samp.getPeriod().multipliedBy(chunk.getBeginPixel())),
                                                            samp,
                                                            seqStart);
            logger.debug("Plot toMSeed begin: "+drList.get(0).getHeader().getStartTime());
            out.addAll(drList);
            seqStart += drList.size();
        }
        return out;
    }
    
    /** Exctract plottables stored in miniseed. Assume all datarecords are in order
     * and from the same channel.
     * @throws FissuresException 
     * @throws SeedFormatException 
     */
    public static List<PlottableChunk> toPlottable(List<DataRecord> drList) throws SeedFormatException, FissuresException {
        List<PlottableChunk> out = new ArrayList<PlottableChunk>();
        for (DataRecord dr : drList) {
            out.add(toPlottable(dr));
        }
        return out;
    }

    public static PlottableChunk toPlottable(DataRecord dr) throws SeedFormatException, FissuresException {
        LocalSeismogramImpl seis = toFissures(new DataRecord[] {dr});
        int[] yData = seis.get_as_longs();
        int[] xData = new int[yData.length];
        for (int i = 0; i < xData.length; i++) {
            xData[i] = i/2;
        }
        Plottable pData = new Plottable(xData, yData);
        int pixelsPerDay = Math.round(((float)DAY.toNanos() / seis.getSampling().getPeriod().toNanos()/2));
        IntRange seisPixelRange = SimplePlotUtil.getDayPixelRange(seis,
                                                   pixelsPerDay,
                                                   seis.getBeginTime());
        PlottableChunk chunk = new PlottableChunk(pData,
                                                  0,  //seisPixelRange.getMin()
                                                  seis.getBeginTime(), 
                                                  pixelsPerDay,
                                                  seis.getChannelID().getNetworkId(),
                                                  seis.getChannelID().getStationCode(),
                                                  seis.getChannelID().getLocCode(),
                                                  seis.getChannelID().getChannelCode());
        logger.debug("chunk "+ChannelIdUtil.toStringNoDates(seis.getChannelID())+" "+chunk.getBeginTime()+" "+chunk.getEndTime()+" "+chunk.getBeginPixel()+" "+chunk.getNumDataPoints()+" "+chunk.getNumPixels()+" "+chunk.getPixelsPerDay());
        return chunk;
    }

    public static SamplingImpl convertSampleRate(DataRecord seed) {
        SamplingImpl sampling;
        Blockette[] blocketts = seed.getBlockettes(100);
        int numPerSampling;
        Duration timeInterval;
        if (blocketts.length != 0) {
            Blockette100 b100 = (Blockette100)blocketts[0];
            float f = b100.getActualSampleRate();
            numPerSampling = 1;
            timeInterval = TimeUtils.durationFromSeconds( f);
            sampling = new SamplingImpl(numPerSampling, timeInterval);
        } else {
            DataHeader header = seed.getHeader();
            sampling = convertSampleRate(header.getSampleRateMultiplier(), header.getSampleRateFactor());
        }
        return sampling;
    }

    public static SamplingImpl convertSampleRate(int multi, int factor) {
        int numPerSampling;
        Duration timeInterval;
        if (factor > 0) {
            numPerSampling = factor;
            timeInterval = TimeUtils.ONE_SECOND;
            if (multi > 0) {
                numPerSampling *= multi;
            } else {
                timeInterval = timeInterval.multipliedBy(-1 * multi);
            }
        } else {
            numPerSampling = 1;
            timeInterval = TimeUtils.ONE_SECOND.multipliedBy(-1 * factor);
            if (multi > 0) {
                numPerSampling *= multi;
            } else {
                timeInterval = timeInterval.multipliedBy(-1 * multi);
            }
        }
        SamplingImpl sampling = new SamplingImpl(numPerSampling, timeInterval);
        return sampling;
    }

    public static TimeSeriesDataSel convertData(DataRecord seed) throws SeedFormatException {
        Blockette1000 b1000 = (Blockette1000)seed.getUniqueBlockette(1000);
        EncodedData eData = new EncodedData(b1000.getEncodingFormat(),
                                            seed.getData(),
                                            seed.getHeader().getNumSamples(),
                                            !b1000.isBigEndian());
        EncodedData[] eArray = new EncodedData[1];
        eArray[0] = eData;
        TimeSeriesDataSel bits = new TimeSeriesDataSel();
        bits.encoded_values(eArray);
        return bits;
    }
    
    public static EncodedData[] toEncodedData(int[] data) {
        // for int (corba calls this a long), 64 bytes = 4 bytes * 16 samples, so each edata
        // holds 62*16 samples
        if (data.length == 0) {
            return new EncodedData[0];
        }
        EncodedData[] eData = new EncodedData[(int)Math.ceil(data.length * 4.0f / (62 * 64))];
        for (int i = 0; i < eData.length; i++) {
            byte[] dataBytes = new byte[62 * 64];
            int j;
            for (j = 0; j + (62 * 16 * i) < data.length && j < 62 * 16; j++) {
                int val = data[j + (62 * 16 * i)];
                dataBytes[4 * j] = (byte)((val & 0xff000000) >> 24);
                dataBytes[4 * j + 1] = (byte)((val & 0x00ff0000) >> 16);
                dataBytes[4 * j + 2] = (byte)((val & 0x0000ff00) >> 8);
                dataBytes[4 * j + 3] = (byte)((val & 0x000000ff));
            }
            eData[i] = new EncodedData((short)B1000Types.INTEGER, dataBytes, j, false);
        }
        return eData;
    }

    /**
     * get the value of start time in ISO format
     * 
     * @return the value of start time in ISO format
     */
    public static String getISOTime(Btime startStruct) {
        return startStruct.toInstant().toString();
    }
    
    
    public static Instant getZonedDateTime(Btime startStruct) {
        return startStruct.toInstant();
    }

    public static Btime getBtime(Instant date) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(date, TimeUtils.TZ_UTC);
        Btime btime = new Btime();
        btime.tenthMilli = zdt.getNano()/100000;
        btime.year = zdt.getYear();
        btime.jday = zdt.getDayOfYear();
        btime.hour = zdt.getHour();
        btime.min = zdt.getMinute();
        btime.sec = zdt.getSecond();
        return btime;
    }
    
    public static TimeRange getTimeRange(BtimeRange bTime) {
        return new TimeRange(bTime.getBegin().toInstant(), bTime.getEnd().toInstant());
    }

    public static final byte RECORD_SIZE_4096_POWER = 12;

    public static int RECORD_SIZE_4096 = (int)Math.pow(2, RECORD_SIZE_4096_POWER);

    public static final byte RECORD_SIZE_1024_POWER = 10;

    public static int RECORD_SIZE_1024 = (int)Math.pow(2, RECORD_SIZE_1024_POWER);
    
    public static final byte RECORD_SIZE_512_POWER = 9;

    public static int RECORD_SIZE_512 = (int)Math.pow(2, RECORD_SIZE_512_POWER);
    
    public static final byte RECORD_SIZE_256_POWER = 8;

    public static int RECORD_SIZE_256 = (int)Math.pow(2, RECORD_SIZE_256_POWER);

    public static final Duration DAY =  Duration.ofDays(1);
    
    /**
     * Turns a UnitImpl into a byte array using Java serialization
     */
    public static byte[] toBytes(UnitImpl obj) {
        ByteArrayOutputStream byteHolder = new ByteArrayOutputStream();
        try {
            ObjectOutputStream fissuresWriter = new ObjectOutputStream(byteHolder);
            fissuresWriter.writeObject(obj);
            return byteHolder.toByteArray();
        } catch(IOException io) {
            throw new RuntimeException("Didn't think it was possible to get an IO exception dealing entirely with in memory streams",
                                       io);
        }
    }

    /**
     * Turns a byte array containing just a serialized UnitImpl object back into
     * an UnitImpl
     */
    public static UnitImpl fromBytes(byte[] bytes) throws IOException {
        UnitImpl impl;
        try {
            impl = (UnitImpl)new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
        } catch(ClassNotFoundException cnf) {
            throw new IllegalArgumentException("The serialized bytes passed to fromBytes must contain a serialized UnitImpl, instead it was a class we couldn't find: "
                    + cnf.getMessage());
        }
        singletonizeUnitBase(impl);
        return impl;
    }

    private static void singletonizeUnitBase(UnitImpl impl) {
        impl.the_unit_base = UnitBase.from_int(impl.the_unit_base.value());
        for (int i = 0; i < impl.elements.length; i++) {
            singletonizeUnitBase((UnitImpl)impl.elements[i]);
        }
    }
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FissuresConvert.class);
} // FissuresConvert

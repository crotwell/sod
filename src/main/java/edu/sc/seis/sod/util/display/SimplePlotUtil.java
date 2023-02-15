package edu.sc.seis.sod.util.display;

import java.awt.Dimension;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.common.UnitRangeImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.Plottable;
import edu.sc.seis.sod.model.seismogram.PlottableChunk;
import edu.sc.seis.sod.model.util.LinearInterp;
import edu.sc.seis.sod.util.time.ClockUtil;

/**
 * SimplePlotUtil.java Created: Thu Jul 8 11:22:02 1999
 * 
 * @author Philip Crotwell, Charlie Groves
 * @version $Id: SimplePlotUtil.java 22054 2011-02-16 16:51:38Z crotwell $
 */
public class SimplePlotUtil {

    /**
     * Creates a plottable with all the data from the seismogram that falls
     * inside of the time range at samplesPerDay. Each pixel in the plottable is
     * of 1/pixelsPerDay days long. Two points are returned for each pixel. The
     * first value is the min value over the time covered in the seismogram, and
     * the second value is the max. The seismogram points in a plottable pixel
     * consist of the first point at or after the start time of the pixel to the
     * last point before the start time of the next pixel.
     */
    public static Plottable makePlottable(LocalSeismogramImpl seis,
                                          int pixelsPerDay)
            throws CodecException {
        TimeRange correctedSeisRange = correctTimeRangeForPixelData(seis,
                                                                               pixelsPerDay);
        int startPoint = getPoint(seis, correctedSeisRange.getBeginTime());
        int endPoint = getPoint(seis, correctedSeisRange.getEndTime());
        IntRange seisPixelRange = getDayPixelRange(seis,
                                                   pixelsPerDay,
                                                   seis.getBeginTime());
        int numPixels = seisPixelRange.getDifference();
        // check to see if numPixels doesn't go over
        Instant rangeEnd = correctedSeisRange.getBeginTime()
                .plus(getPixelPeriod(pixelsPerDay).multipliedBy(numPixels));
        boolean corrected = false;
        if(rangeEnd.isAfter(correctedSeisRange.getEndTime())) {
            numPixels--;
            corrected = true;
        }
        // end check and correction
        int startPixel = seisPixelRange.getMin();
        int[][] pixels = new int[2][numPixels * 2];
        int pixelPoint = startPixel < 0 ? 0 : startPoint;
        Instant pixelEndTime = correctedSeisRange.getBeginTime();
        Duration pixelPeriod = getPixelPeriod(pixelsPerDay);
        if(corrected) {}
        for(int i = 0; i < numPixels; i++) {
            pixelEndTime = pixelEndTime.plus(pixelPeriod);
            int pos = 2 * i;
            int nextPos = pos + 1;
            pixels[0][pos] = startPixel + i;
            pixels[0][nextPos] = pixels[0][pos];
            int nextPixelPoint = getPixel(startPoint,
                                          endPoint,
                                          correctedSeisRange.getBeginTime(),
                                          correctedSeisRange.getEndTime(),
                                          pixelEndTime);
            QuantityImpl min = seis.getMinValue(pixelPoint, nextPixelPoint);
            pixels[1][pos] = (int)min.getValue();
            QuantityImpl max = seis.getMaxValue(pixelPoint, nextPixelPoint);
            pixels[1][nextPos] = (int)max.getValue();
            if(corrected && (i < 2 || i >= numPixels - 2)) {
                logger.debug(pixels[0][pos] + ": min " + min.getValue() + " max "
                        + max.getValue());
            }
            pixelPoint = nextPixelPoint;
        }
        return new Plottable(pixels[0], pixels[1]);
    }

    public static Plottable getEmptyPlottable() {
        int[] empty = new int[0];
        return new Plottable(empty, empty);
    }

    public static void debugExtraPixel(TimeRange correctedSeisRange,
                                       Instant rangeEnd,
                                       LocalSeismogramImpl seis,
                                       int startPoint,
                                       int endPoint,
                                       int numPixels,
                                       IntRange seisPixelRange,
                                       int startPixel,
                                       Duration pixelPeriod) {
        logger.warn("corrected for freak extra pixel!");
        logger.debug("correctedSeisRange: " + correctedSeisRange);
        logger.debug("end of range would have been " + rangeEnd
                + " without correction");
        logger.debug("seis.num_points: " + seis.num_points);
        logger.debug("startPoint: " + startPoint);
        logger.debug("endPoint: " + endPoint);
        logger.debug("seisPixelRange: " + seisPixelRange);
        logger.debug("numPixels after correction: " + numPixels);
        logger.debug("startPixel: " + startPixel);
        logger.debug("pixelPeriod: " + pixelPeriod);
    }

    public static Duration getPixelPeriod(int pixelsPerDay) {
        double pixelPeriod = 1.0 / (double)pixelsPerDay;
        return ClockUtil.durationFrom(pixelPeriod, UnitImpl.DAY);
    }

    public static Instant getBeginningOfDay(Instant date) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(date, TimeUtils.TZ_UTC);
        zdt = ZonedDateTime.of(zdt.getYear(), zdt.getMonthValue(), zdt.getDayOfMonth(), 0, 0, 0, 0, TimeUtils.TZ_UTC);
        return zdt.toInstant();
    }

    public static TimeRange getDay(Instant date) {
        return new TimeRange(getBeginningOfDay(date), ONE_DAY);
    }

    public static Instant getPixelBeginTime(TimeRange day,
                                                    int pixel,
                                                    int pixelsPerDay) {
        Duration pixelPeriod = getPixelPeriod(pixelsPerDay);
        return day.getBeginTime()
                .plus(pixelPeriod.multipliedBy(pixel));
    }

    /*
     * gets the time range that makes up one pixel of plottable data that either
     * surrounds the given date or is directly after the given date
     */
    public static TimeRange getPixelTimeRange(Instant point,
                                                         int pixelsPerDay,
                                                         boolean after) {
        Duration pixelPeriod = getPixelPeriod(pixelsPerDay);
        TimeRange day = getDay(point);
        int pixel = getPixel(pixelsPerDay, day, point);
        if(after) {
            pixel++;
        }
        Instant pixelBegin = getPixelBeginTime(day, pixel, pixelsPerDay);
        return new TimeRange(pixelBegin, pixelPeriod);
    }

    /*
     * Gets the pixel range of the seismogram from the point of view of the
     * beginning (midnight) of the day of the begin time of the seismogram. This
     * is to say that if you have an two-hour-long seismogram starting at noon
     * on a day with a resolution of 12 pixels per day, the range returned would
     * be 6 to 7.
     */
    public static IntRange getDayPixelRange(LocalSeismogramImpl seis,
                                            int pixelsPerDay) {
        return getDayPixelRange(seis,
                                pixelsPerDay,
                                getBeginningOfDay(seis.begin_time));
    }


    /*
     * Same as above, except day can start at any time. The pixel time
     * boundaries are still dependent upon midnight of the seismogram start
     * time.
     */
    public static IntRange getDayPixelRange(LocalSeismogramImpl seis,
                                            int pixelsPerDay,
                                            Instant startOfDay) {
        TimeRange seisTR = new TimeRange((LocalSeismogramImpl)seis);
        TimeRange dayTR = new TimeRange(startOfDay,
                                                              ONE_DAY);
        int startPixel = getPixel(pixelsPerDay, dayTR, seisTR.getBeginTime());
        if(getPixelTimeRange(seisTR.getBeginTime(), pixelsPerDay, false).getBeginTime()
                .isBefore(seisTR.getBeginTime())) {
            // we don't want pixels with partial data
            startPixel++;
        }
        int endPixel = getPixel(pixelsPerDay, dayTR, seisTR.getEndTime());
        if(endPixel < startPixel) {
            // yes, this pretty much means the difference of the pixel range
            // will be 0
            endPixel = startPixel;
        }
        return new IntRange(startPixel, endPixel);
    }

    public static boolean canMakeAtLeastOnePixel(LocalSeismogramImpl seis,
                                                 int pixelsPerDay) {
        IntRange pixelRange = getDayPixelRange(seis, pixelsPerDay);
        return pixelRange.getMax() > pixelRange.getMin();
    }

    public static TimeRange correctTimeRangeForPixelData(LocalSeismogramImpl seis,
                                                                    int pixelsPerDay) {
        IntRange pixelRange = getDayPixelRange(seis, pixelsPerDay);
        TimeRange day = getDay(seis.begin_time);
        Instant start = getPixelBeginTime(day,
                                                  pixelRange.getMin(),
                                                  pixelsPerDay);
        Instant end = getPixelBeginTime(day,
                                                pixelRange.getMax(),
                                                pixelsPerDay);
        return new TimeRange(start, end);
    }

    public static int[][] compressXvalues(LocalSeismogramImpl seismogram,
                                          TimeRange timeRange,
                                          Dimension size) throws CodecException {
        LocalSeismogramImpl seis = (LocalSeismogramImpl)seismogram;
        int width = size.width;
        int[][] out = new int[2][];
        if(seis.getEndTime().isBefore(timeRange.getBeginTime())
                || seis.getBeginTime().isAfter(timeRange.getEndTime())) {
            out[0] = new int[0];
            out[1] = new int[0];
            logger.info("The end time is before the beginTime in simple seismogram");
            return out;
        }
        Instant tMin = timeRange.getBeginTime();
        Instant tMax = timeRange.getEndTime();
        int seisStartIndex = getPoint(seis, tMin);
        int seisEndIndex = getPoint(seis, tMax);
        if(seisStartIndex < 0) {
            seisStartIndex = 0;
        }
        if(seisEndIndex >= seis.getNumPoints()) {
            seisEndIndex = seis.getNumPoints() - 1;
        }
        Instant tempdate = getValue(seis.getNumPoints(),
                                            seis.getBeginTime(),
                                            seis.getEndTime(),
                                            seisStartIndex);
        int pixelStartIndex = getPixel(width, timeRange, tempdate);
        tempdate = getValue(seis.getNumPoints(),
                            seis.getBeginTime(),
                            seis.getEndTime(),
                            seisEndIndex);
        int pixelEndIndex = getPixel(width, timeRange, tempdate);
        int pixels = seisEndIndex - seisStartIndex + 1;
        out[0] = new int[2 * pixels];
        out[1] = new int[out[0].length];
        int tempYvalues[] = new int[out[0].length];
        int seisIndex = seisStartIndex;
        int numAdded = 0;
        int xvalue = Math.round((float)(linearInterp(seisStartIndex,
                                                     pixelStartIndex,
                                                     seisEndIndex,
                                                     pixelEndIndex,
                                                     seisIndex)));
        int tempValue = 0;
        seisIndex++;
        int j = 0;
        while(seisIndex <= seisEndIndex) {
            tempValue = Math.round((float)(linearInterp(seisStartIndex,
                                                        pixelStartIndex,
                                                        seisEndIndex,
                                                        pixelEndIndex,
                                                        seisIndex)));
            tempYvalues[j++] = (int)seis.getValueAt(seisIndex).getValue();
            if(tempValue != xvalue) {
                out[0][numAdded] = xvalue;
                out[0][numAdded + 1] = xvalue;
                out[1][numAdded] = getMinValue(tempYvalues, 0, j - 1);
                out[1][numAdded + 1] = getMaxValue(tempYvalues, 0, j - 1);
                j = 0;
                xvalue = tempValue;
                numAdded = numAdded + 2;
            }
            seisIndex++;
        }
        int temp[][] = new int[2][numAdded];
        System.arraycopy(out[0], 0, temp[0], 0, numAdded);
        System.arraycopy(out[1], 0, temp[1], 0, numAdded);
        return temp;
    }

    private static int getMinValue(int[] yValues, int startIndex, int endIndex) {
        int minValue = java.lang.Integer.MAX_VALUE;
        for(int i = startIndex; i <= endIndex; i++) {
            if(yValues[i] < minValue)
                minValue = yValues[i];
        }
        return minValue;
    }

    private static int getMaxValue(int[] yValues, int startIndex, int endIndex) {
        int maxValue = java.lang.Integer.MIN_VALUE;
        for(int i = startIndex; i <= endIndex; i++) {
            if(yValues[i] > maxValue)
                maxValue = yValues[i];
        }
        return maxValue;
    }

    /**
     * solves the equation (yb-ya)/(xb-xa) = (y-ya)/(x-xa) for y given x. Useful
     * for finding the pixel for a value given the dimension of the area and the
     * range of values it is supposed to cover. Note, this does not check for xa ==
     * xb, in which case a divide by zero would occur.
     */
    @Deprecated
    public static final double linearInterp(double xa,
                                            double ya,
                                            double xb,
                                            double yb,
                                            double x) {
        return LinearInterp.linearInterp(xa, ya, xb, yb, x);
    }

    public static final int getPixel(int totalPixels,
                                     TimeRange tr,
                                     Instant value) {
        return getPixel(totalPixels, tr.getBeginTime(), tr.getEndTime(), value);
    }

    public static final int getPoint(LocalSeismogramImpl seis,
                                     Instant time) {
        return getPixel(seis.getNumPoints(),
                        seis.getBeginTime(),
                        seis.getEndTime(),
                        time);
    }

    public static final int getPixel(int totalPixels,
                                     Instant begin,
                                     Instant end,
                                     Instant value) {
        return getPixel(0, totalPixels, begin, end, value);
    }

    public static final int getPixel(int startPixel,
                                     int endPixel,
                                     Instant begin,
                                     Instant end,
                                     Instant value) {
        return (int)linearInterp(TimeUtils.instantToEpochSeconds(begin),
                                 startPixel,
                                 TimeUtils.instantToEpochSeconds(end),
                                 endPixel,
                                 TimeUtils.instantToEpochSeconds(value));
    }

    public static final Instant getValue(int totalPixels,
                                         Instant begin,
                                         Instant end,
                                                 int pixel) {
        return getValue(0, totalPixels, begin, end, pixel);
    }

    public static final Instant getValue(int startPixel,
                                                 int endPixel,
                                                 Instant begin,
                                                 Instant end,
                                                 int pixel) {
        double value = linearInterp(startPixel,
                                    0,
                                    endPixel,
                                    Duration.between(begin, end).toNanos(),
                                    pixel);
        Duration d = Duration.ofNanos(Math.round(value));
        return begin.plus(d);
    }

    public static final int getPixel(int totalPixels,
                                     UnitRangeImpl range,
                                     QuantityImpl value) {
        QuantityImpl converted = value.convertTo(range.getUnit());
        return getPixel(totalPixels, range, converted.getValue());
    }

    public static final int getPixel(int totalPixels,
                                     UnitRangeImpl range,
                                     double value) {
        return (int)linearInterp(range.getMinValue(),
                                 0,
                                 range.getMaxValue(),
                                 totalPixels,
                                 value);
    }

    public static final QuantityImpl getValue(int totalPixels,
                                              UnitRangeImpl range,
                                              int pixel) {
        double value = linearInterp(0,
                                    range.getMinValue(),
                                    totalPixels,
                                    range.getMaxValue(),
                                    pixel);
        return new QuantityImpl(value, range.getUnit());
    }

    public static final Instant getTimeForIndex(int index,
                                                Instant beginTime,
                                                        SamplingImpl sampling) {
        Duration width = sampling.getPeriod();
        width = width.multipliedBy(index);
        return beginTime.plus(width);
    }

    public static final Duration ONE_DAY = TimeUtils.ONE_DAY;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SimplePlotUtil.class);

    public static List<PlottableChunk> makePlottables(LocalSeismogramImpl[] seis, int pixelsPerDay)
            throws IOException {
        List<PlottableChunk> chunks = new ArrayList<PlottableChunk>();
        for (int i = 0; i < seis.length; i++) {
            LocalSeismogramImpl curSeis = (LocalSeismogramImpl)seis[i];
            if (curSeis.getNumPoints() > 0 && canMakeAtLeastOnePixel(seis[i], pixelsPerDay)) {
                try {
                    Plottable plott = makePlottable(curSeis, pixelsPerDay);
                    if (plott.x_coor.length > 0) {
                        Instant plotStartTime = getBeginningOfDay(curSeis.getBeginTime());
                        PlottableChunk chunk = new PlottableChunk(plott,
                                                                  getDayPixelRange(seis[i],
                                                                                                  pixelsPerDay,
                                                                                                  plotStartTime)
                                                                          .getMin(),
                                                                  plotStartTime,
                                                                  pixelsPerDay,
                                                                  curSeis.channel_id.getNetworkId(),
                                                                  curSeis.channel_id.getStationCode(),
                                                                  curSeis.channel_id.getLocCode(),
                                                                  curSeis.channel_id.getChannelCode());
                        chunks.add(chunk);
                    }
                } catch(CodecException e) {
                    logger.warn("unable to make plottable for "+curSeis+", skipping.", e);
                }
            }
        }
        return chunks;
    }

    public static List<PlottableChunk> convertToCommonPixelScale(List<PlottableChunk> chunks,
                                                                 TimeRange requestRange,
                                                             int pixelsPerDay) {
        int requestPixels = getPixels(pixelsPerDay, requestRange);
        List<PlottableChunk> outChunks = new ArrayList<PlottableChunk>();
        Iterator<PlottableChunk> it = chunks.iterator();
        while (it.hasNext()) {
            PlottableChunk pc = it.next();
            if (pc.getEndTime().isBefore(requestRange.getBeginTime())) {
                // whole chunk before request
                continue;
            }
            Instant rowBeginTime = pc.getBeginTime();
            int offsetIntoRequestPixels = getPixel(requestPixels, requestRange, rowBeginTime);
            int numPixels = pc.getNumPixels();
            int firstPixelForRequest = 0;
            if (offsetIntoRequestPixels < 0) {
                // This db row has data starting before the request, start
                // at
                // pertinent point
                firstPixelForRequest = -1 * offsetIntoRequestPixels;
            }
            int lastPixelForRequest = numPixels;
            if (offsetIntoRequestPixels + numPixels > requestPixels) {
                // This row has more data than was requested in it, only get
                // enough to fill the request
                lastPixelForRequest = requestPixels - offsetIntoRequestPixels;
            }
            if (firstPixelForRequest > lastPixelForRequest) {
                throw new NegativeArraySizeException("first pixel > last pixel: f="+firstPixelForRequest+"  l="+lastPixelForRequest);
            }
            int pixelsUsed = lastPixelForRequest - firstPixelForRequest;
            int[] x = new int[pixelsUsed * 2];
            int[] y = new int[pixelsUsed * 2];
            int[] ploty = pc.getYData();
            System.arraycopy(ploty, firstPixelForRequest*2, y, 0, pixelsUsed*2);
            for (int i = 0; i < pixelsUsed * 2; i++) {
                x[i] = firstPixelForRequest + offsetIntoRequestPixels + i / 2;
            }
            Plottable p = new Plottable(x, y);
            PlottableChunk shiftPC = new PlottableChunk(p,
                                                        getPixel(rowBeginTime, pixelsPerDay)
                                                                + firstPixelForRequest,
                                                        PlottableChunk.getJDay(rowBeginTime),
                                                        PlottableChunk.getYear(rowBeginTime),
                                                        pixelsPerDay,
                                                        pc.getNetworkCode(),
                                                        pc.getStationCode(),
                                                        pc.getSiteCode(),
                                                        pc.getChannelCode());
            outChunks.add(shiftPC);
        }
        return outChunks;
    }

    public static int getPixels(int pixelsPerDay, TimeRange tr) {
        Duration inter = tr.getInterval();
        double interDays = inter.toNanos()/(86400*TimeUtils.NANOS_IN_SEC);
        double samples = pixelsPerDay * interDays;
        return (int)Math.floor(samples);
    }
    
    // from PlottableChunk
    public static int getPixel(Instant time, int pixelsPerDay) {
        Instant day = PlottableChunk.stripToDay(time);
        TimeRange tr = new TimeRange(day, ONE_DAY);
        double pixel = SimplePlotUtil.getPixel(pixelsPerDay, tr, time);
        return (int)Math.floor(pixel);
    }
    

} // SimplePlotUtil

package edu.sc.seis.sod.model.seismogram;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.util.LinearInterp;

/**
 * @author groves Created on Oct 18, 2004
 */
public class PlottableChunk {

    /** for hibernate */
    protected PlottableChunk() {}

    /**
     * Creates a plottable chunk consisting of the plottable in data, starting
     * start pixels into the jday and year of otherstuff at
     * otherstuff.getPixelsPerDay ppd.
     */
    public PlottableChunk(Plottable data,
                          int startPixel,
                          PlottableChunk otherStuff) {
        this(data,
             startPixel,
             otherStuff.getJDay(),
             otherStuff.getYear(),
             otherStuff.getPixelsPerDay(),
             otherStuff.getNetworkCode(),
             otherStuff.getStationCode(),
             otherStuff.getSiteCode(),
             otherStuff.getChannelCode());
    }

    /**
     * Creates a plottable chunk based on the plottable in data, starting
     * startPixel pixels into the jday and year of start data at pixelsPerDay
     * NOTE: The start pixel should be relative to the beginning of the jday of
     * the start date. Otherwise, things get screwy.
     */
    public PlottableChunk(Plottable data,
                          int startPixel,
                          Instant startDate,
                          int pixelsPerDay,
                          String networkCode,
                          String stationCode,
                          String siteCode,
                          String channelCode) {
        this(data,
             startPixel,
             getJDay(startDate),
             getYear(startDate),
             pixelsPerDay,
             networkCode,
             stationCode,
             siteCode,
             channelCode);
    }

    /**
     * Creates a plottable chunk based on the plottable in data, starting
     * startPixel pixels into the jday and year at pixelsPerDay
     */
    public PlottableChunk(Plottable data,
                          int startPixel,
                          int jday,
                          int year,
                          int pixelsPerDay,
                          String networkCode,
                          String stationCode,
                          String siteCode,
                          String channelCode) {
        this.data = data;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(out));
            for(int k = 0; k < data.y_coor.length; k++) {
                dos.writeInt(data.y_coor[k]);
            }
            dos.close();
        } catch(IOException e) {
            throw new RuntimeException("Should never happen with a ByteArrayOutputStream", e);
        }
        yBytes = out.toByteArray();
        
        // here we shall get rid of days of dead space if they exist
        if(startPixel >= pixelsPerDay) {
            int numDaysToAdd = startPixel / pixelsPerDay;
            Instant date = getDate(jday, year);
            date = date.plus(Duration.ofDays(numDaysToAdd));
            jday = getJDay(date);
            year = getYear(date);
            startPixel = startPixel % pixelsPerDay;
        }
        this.beginPixel = startPixel;
        this.pixelsPerDay = pixelsPerDay;
        this.numDataPoints = data.y_coor.length;
        this.jday = jday;
        this.year = year;
        this.networkCode = networkCode;
        this.stationCode = stationCode;
        this.locCode = siteCode;
        this.channelCode = channelCode;
    }

    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(o instanceof PlottableChunk) {
            PlottableChunk oChunk = (PlottableChunk)o;
            if(networkCode.equals(oChunk.networkCode) &&
                    stationCode.equals(oChunk.stationCode) &&
                    locCode.equals(oChunk.locCode) &&
                    channelCode.equals(oChunk.channelCode) &&
                    pixelsPerDay == oChunk.pixelsPerDay &&
                    jday == oChunk.jday &&
                    year == oChunk.year &&
                    getNumDataPoints() == oChunk.getNumDataPoints()) {
                return true;
            }
        }
        return false;
    }

    public static Instant getDate(int jday, int year) {
        return ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, TimeUtils.TZ_UTC).plusDays(jday-1).toInstant();
    }

    public static Instant getTime(int pixel,
                                          int jday,
                                          int year,
                                          int pixelsPerDay) {
        Instant dayBegin = getDate(jday, year);
        double sampleMillis = LinearInterp.linearInterp(0,
                                                          0,
                                                          pixelsPerDay,
                                                          MILLIS_IN_DAY,
                                                          pixel);
        sampleMillis = Math.floor(sampleMillis);
        return dayBegin.plusMillis(Math.round(sampleMillis));
    }

    public static int getJDay(Instant time) {
        return time.get(ChronoField.DAY_OF_YEAR);
    }

    public static int getYear(Instant time) {
        return time.get(ChronoField.YEAR);
    }

    public static Instant stripToDay(Instant d) {
        return d.truncatedTo(ChronoUnit.DAYS);
    }
    
    private static final int MILLIS_IN_DAY = 24 * 60 * 60 * 1000;

    public static final Duration ONE_DAY = Duration.ofDays(1);
    
    public static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");

    public Plottable getData() {
        synchronized(this) {
            if (data == null) {
                int[] yValues = getYData();
                int[] xValues = new int[yValues.length];
                for (int i = 0; i < xValues.length; i++) {
                    xValues[i] = i/2;
                }
                data = new Plottable(xValues, yValues);
            }
        }
        return data;
    }

    public int getPixelsPerDay() {
        return pixelsPerDay;
    }

    public int getBeginPixel() {
        return beginPixel;
    }

    public int getNumPixels() {
        return getNumDataPoints() / 2;
    }

    public Instant getTime(int pixel) {
        return getTime(pixel, getJDay(), getYear(), getPixelsPerDay());
    }

    public Instant getBeginTime() {
        return getTime(beginPixel);
    }

    public Instant getEndTime() {
        return getTime(getBeginPixel() + getNumPixels());
    }

    public TimeRange getTimeRange() {
        return new TimeRange(getBeginTime(), getEndTime());
    }

    public int getJDay() {
        return jday;
    }

    public int getYear() {
        return year;
    }

    public int hashCode() {
        int hashCode = 81 + networkCode.hashCode();
        hashCode = 37 * hashCode + stationCode.hashCode();
        hashCode = 37 * hashCode + locCode.hashCode();
        hashCode = 37 * hashCode + channelCode.hashCode();
        hashCode = 37 * hashCode + pixelsPerDay;
        hashCode = 37 * hashCode + jday;
        hashCode = 37 * hashCode + year;
        return 37 * hashCode + getNumDataPoints();
    }

    public String toString() {
        return getNumPixels() + " pixel chunk from "
                + networkCode + "."
                + stationCode + "." + locCode + "." + channelCode + " at "
                + pixelsPerDay + " ppd from " + getTimeRange();
    }

    public List<PlottableChunk> breakIntoDays() {
        int numDays = (int)Math.ceil((beginPixel + getNumPixels())
                / (double)getPixelsPerDay());
        List<PlottableChunk> dayChunks = new ArrayList<PlottableChunk>();
        Instant time = getBeginTime();
        for(int i = 0; i < numDays; i++) {
            int firstDayPixels = pixelsPerDay - getBeginPixel();
            int startPixel = (i - 1) * pixelsPerDay + firstDayPixels;
            int stopPixel = i * pixelsPerDay + firstDayPixels;
            int pixelIntoNewDay = 0;
            if(i == 0) {
                startPixel = 0;
                stopPixel = firstDayPixels;
                pixelIntoNewDay = getBeginPixel();
            }
            if(i == numDays - 1) {
                stopPixel = getNumPixels();
            }
            int[] y = new int[(stopPixel - startPixel) * 2];
            System.arraycopy(getYData(), startPixel * 2, y, 0, y.length);
            Plottable p = new Plottable(null, y);
            dayChunks.add(new PlottableChunk(p,
                                             pixelIntoNewDay,
                                             getJDay(time),
                                             getYear(time),
                                             getPixelsPerDay(),
                                             getNetworkCode(),
                                             getStationCode(),
                                             getSiteCode(),
                                             getChannelCode()));
            time = time.plus(ONE_DAY);
        }
        return dayChunks;
    }

    // hibernate

    protected void setData(Plottable data) {
        this.data = data;
    }

    protected void setPixelsPerDay(int pixelsPerDay) {
        this.pixelsPerDay = pixelsPerDay;
    }

    protected void setBeginPixel(int beginPixel) {
        this.beginPixel = beginPixel;
    }

    protected void setJday(int jday) {
        this.jday = jday;
    }

    protected void setYear(int year) {
        this.year = year;
    }

    public long getDbid() {
        return dbid;
    }

    protected void setDbid(long dbid) {
        this.dbid = dbid;
    }
    
    protected Timestamp getBeginTimestamp() {
        return Timestamp.from(getBeginTime());
    }
    
    protected void setBeginTimestamp(Timestamp begin) {
        Instant msd = begin.toInstant();
        year = msd.get(ChronoField.YEAR);
        jday = msd.get(ChronoField.DAY_OF_YEAR);
    }
    
    protected Timestamp getEndTimestamp() {
        return Timestamp.from(getEndTime());
    }
    
    protected void setEndTimestamp(Timestamp begin) {
        // this is generated from begin and pixels, so no need to set
        // have this method as a no-op so hibernate doesn't get mad
    }

    private long dbid;

    private String networkCode;
    private String stationCode;
    private String locCode;
    private String channelCode;

    private byte[] yBytes;
    
    private transient Plottable data = null;

    private int pixelsPerDay, beginPixel, numDataPoints;

    private int jday, year;

    private static final Logger logger = LoggerFactory.getLogger(PlottableChunk.class);

    
    public String getNetworkCode() {
        return networkCode;
    }

    
    public void setNetworkCode(String networkCode) {
        this.networkCode = networkCode;
    }

    
    public String getStationCode() {
        return stationCode;
    }

    
    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    
    public String getSiteCode() {
        return locCode;
    }

    
    public void setSiteCode(String siteCode) {
        this.locCode = siteCode;
    }

    
    public String getChannelCode() {
        return channelCode;
    }

    
    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public byte[] getYBytes() {
        return yBytes;
    }

    protected void setYBytes(byte[] bytes) {
        yBytes = bytes;
    }
    
    public int[] getYData() {
        return toIntArray(getYBytes());
    }
    
    private int[] toIntArray(byte[] bytes) {
        try {
            DataInputStream dis = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)));
            int[] decomp = new int[getNumDataPoints()];
            for (int i = 0; i < decomp.length; i++) {
                decomp[i] = dis.readInt();
            }
            return decomp;
        } catch(IOException e) {
            // cant happen
            throw new RuntimeException("Should never happen", e);
        }
    }

    
    public int getNumDataPoints() {
        return numDataPoints;
    }

    
    protected void setNumDataPoints(int numDataPoints) {
        this.numDataPoints = numDataPoints;
    }
}

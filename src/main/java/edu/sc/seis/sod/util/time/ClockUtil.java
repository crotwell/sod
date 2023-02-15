package edu.sc.seis.sod.util.time;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;

/**
 * ClockUtil.java Created: Mon Mar 17 09:34:25 2003
 * 
 * @author Philip Crotwell
 */
public class ClockUtil {

    /**
     * Calculates the difference between the CPU clock and the time retrieved
     * from the http://www.seis.sc.edu/cgi-bin/date_time.pl. 
     */
    public static Duration getTimeOffset() {
        if(serverOffset == null ) {
            if (warnServerFail ) {
                // already tried and failed, so...
                return ZERO_OFFSET;
            }
            try {
                serverOffset = getServerTimeOffset();
            } catch(Throwable e) {
            	noGoClock(e);
                return ZERO_OFFSET;
            } // end of try-catch
        } // end of if ()
        return serverOffset;
    }
    
    private static void noGoClock(Throwable e) {
        warnServerFail = true;
        // oh well, can't get to server, use CPU time, so
        // offset is zero, check for really bad clocks first
        logger.debug("Unable to make a connection to "+SEIS_SC_EDU_URL+" to verify system clock, assuming offset is zero.", e);
        logger.warn("Unable to make a connection to "+SEIS_SC_EDU_URL+" to verify system clock, assuming offset is zero.");
        Instant localNow = Instant.now();
        if(!warnBadBadClock && OLD_DATE.isAfter(localNow)) {
            warnBadBadClock = true;
            GlobalExceptionHandler.handle("Unable to check the time from the server and the computer's clock is obviously wrong. Please reset the clock on your computer to be closer to real time. \nComputer Time="
                                                  + localNow
                                                  + "\nTime checking url="
                                                  + SEIS_SC_EDU_URL,
                                          e);
        }
    }

    /**
     * Creates a new MicroSecondDate that reflects the current time to the best
     * ability of the system. If a connection to a remote server cannot be
     * established, then the current CPU time is used.
     */
    public static Instant now() {
        return Instant.now().plus(getTimeOffset());
    }

    public static Instant tomorrow() {
        return now().plus(TimeUtils.ONE_DAY);
    }
    
    public static Instant yesterday() {
        return now().minus(TimeUtils.ONE_DAY);
    }
    
    public static Instant lastWeek() {
        return now().minus(TimeUtils.ONE_WEEK);
    }
    
    public static Instant lastMonth() {
        return now().minus(TimeUtils.ONE_MONTH);
    }
    
    @Deprecated
    public static Instant wayPast() {
        return TimeUtils.wayPast;
    }
    
    /** see future in TimeUtils in seisFile */
    @Deprecated
    public static Instant wayFuture() {
        return TimeUtils.future;
    }

    public static Duration getServerTimeOffset() throws IOException {
        HttpURLConnection conn = (HttpURLConnection)SEIS_SC_EDU_URL.openConnection();
        conn.setReadTimeout(10000); // timeout after 10 seconds
        InputStream is = conn.getInputStream();
        InputStreamReader isReader = new InputStreamReader(is);
        BufferedReader bufferedReader = new BufferedReader(isReader);
        String str;
        String timeStr = null;
        while((str = bufferedReader.readLine()) != null) {
            timeStr = str;
        }
        Instant localTime = Instant.now();
        Instant serverTime = TimeUtils.parseISOString(timeStr);
        return Duration.between(localTime, serverTime);
    }
    
    /** True if the first duration is less than the second. */
    public static boolean lessThan(Duration first, Duration second) {
        return first.compareTo(second) < 0;
    }
    
    public static Instant parseISOString(String time) {
        return TimeUtils.parseISOString(time);
    }
    
    public static String formatDuration(Instant before, Instant after) {
        return Duration.between(before,  after).toMillis()/1000f+" sec";
    }

    
    public static Duration difference(Instant before, Instant after) {
        return Duration.between(before,  after).abs();
    }
    
    public static String format(Duration d) {
        return d.toString();
    }
    
    public static float floatSeconds(Duration d) {
    	return (float) doubleSeconds(d);
    }
    
    public static double doubleSeconds(Duration d) {
    	return d.getSeconds()+d.getNano()/1000000000.0;
    }
    
    public static Duration durationOfSeconds(double value) {
    	long seconds = (long)Math.floor(value);
    	long remainder = Math.round((value - seconds)*NANO);
    	return Duration.ofSeconds(seconds, remainder);
    }
    
    public static Duration durationFrom(double value, UnitImpl unit) {
        QuantityImpl q = new QuantityImpl(value, unit);
        return Duration.ofNanos(Math.round(q.getValue(UnitImpl.NANOSECOND)));
    }
    
    public static Duration durationFrom(QuantityImpl q) {
        return Duration.ofNanos(Math.round(q.getValue(UnitImpl.NANOSECOND)));
    }
    
    public static final long NANO = 1000000000;
    
    private static boolean warnServerFail = false;

    private static boolean warnBadBadClock = false;

    private static Duration serverOffset = null;

    private static final Duration ZERO_OFFSET = Duration.ofNanos(0);

    private static URL SEIS_SC_EDU_URL;
    static {
        // we have to do this in a static block because of the exception
        try {
            SEIS_SC_EDU_URL = new URL("http://www.seis.sc.edu/cgi-bin/date_time.pl");
        } catch(MalformedURLException e) {
            // Can't happen
            GlobalExceptionHandler.handle("Caught MalformedURL with seis data_time.pl URL. This should never happen.",
                                          e);
        } // end of try-catch
    }

    /** Used to check for really obviously wrong system clocks, set to a day prior to the release date. */
    private static Instant OLD_DATE = TimeUtils.parseISOString("2017-08-14T00:00:00.000Z");

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ClockUtil.class);
    
} // ClockUtil

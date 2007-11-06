package edu.sc.seis.sod;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Timestamp;

import edu.sc.seis.fissuresUtil.chooser.ClockUtil;

public class SodConfig {

    private int dbid;

    protected String config;

    protected Timestamp time;

    /** for hibernate */
    protected SodConfig() {}
    
    public SodConfig(String conf) {
        this.config = conf;
        this.time = ClockUtil.now().getTimestamp();
    }
    
    public SodConfig(BufferedReader r1)
            throws IOException {
        this(extractConfigString(r1));
    }

    public int getDbid() {
        return dbid;
    }

    protected void setDbid(int dbid) {
        this.dbid = dbid;
    }

    public String getConfig() {
        return config;
    }

    protected void setConfig(String config) {
        this.config = config;
    }

    public Timestamp getTime() {
        return time;
    }

    protected void setTime(Timestamp time) {
        this.time = time;
    }

    private static String extractConfigString(BufferedReader r1)
            throws IOException {
        StringBuffer buf = new StringBuffer();
        String line;
        while((line = r1.readLine()) != null) {
            buf.append(line);
        }
        r1.close();
        return buf.toString();
    }
}

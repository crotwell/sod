package edu.sc.seis.sod.hibernate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;

public abstract class AbstractSeismogramFileReference {

    protected int dbid;

    /** just for hibernate */
    protected AbstractSeismogramFileReference() {}
    
    public AbstractSeismogramFileReference(String netCode,
                                           String staCode,
                                           String siteCode,
                                           String chanCode,
                                           Instant beginTime,
                                           Instant endTime,
                                           String filePath,
                                           int fileType) {
        super();
        this.netCode = netCode;
        this.staCode = staCode;
        this.locCode = siteCode;
        this.chanCode = chanCode;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.filePath = filePath;
        this.fileType = fileType;
    }
    
    public String getNetworkCode() {
        return netCode;
    }

    public String getStationCode() {
        return staCode;
    }

    public String getLocCode() {
        return locCode;
    }

    public String getChannelCode() {
        return chanCode;
    }

    public Instant getBeginTime() {
        return beginTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public String getFilePath() {
        return filePath;
    }
    
    public URL getFilePathAsURL() {
        try {
            return new File(getFilePath()).toURI().toURL();
        } catch(MalformedURLException e) {
            throw new RuntimeException("Should not happen as url comes from file.", e);
        }
    }

    public int getFileType() {
        return fileType;
    }

    protected void setNetworkCode(String netCode) {
        this.netCode = netCode;
    }

    protected void setStationCode(String staCode) {
        this.staCode = staCode;
    }

    protected void setLocCode(String locCode) {
        this.locCode = locCode;
    }

    protected void setChannelCode(String chanCode) {
        this.chanCode = chanCode;
    }

    protected void setBeginTime(Instant beginTime) {
        this.beginTime = beginTime;
    }

    protected void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    protected void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public int getDbid() {
        return dbid;
    }

    protected void setDbid(int dbid) {
        this.dbid = dbid;
    }

    protected String netCode;
    protected String staCode;
    protected String locCode;
    protected String chanCode;
    protected Instant beginTime;
    protected Instant endTime;
    protected String filePath;
    protected int fileType;
}
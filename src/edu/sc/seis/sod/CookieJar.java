package edu.sc.seis.sod;

import java.util.*;

/**
 * CookieJar.java
 *
 *
 * Created: Thu Dec 13 18:18:48 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class CookieJar {

    public CookieJar (){
	eventCookies = new HashMap();
	stationCookies = new HashMap();
	channelCookies = new HashMap();
	eventStationCookies = new HashMap();
	seismogramCookies = new HashMap();
    }

    public Object getEventCookie(Object key) {
	return eventCookies.get(key);
    }

    public Object getStationCookie(Object key) {
	return stationCookies.get(key);
    }

    public Object getChannelCookie(Object key) {
	return channelCookies.get(key);
    }

    public Object getEventStationCookie(Object key) {
	return eventStationCookies.get(key);
    }

    public Object getSeismogramCookie(Object key) {
	return seismogramCookies.get(key);
    }

    public void addEventCookie(Object key, Object value) {
	eventCookies.put(key, value);
    }

    public void addStationCookie(Object key, Object value) {
	stationCookies.put(key, value);
    }

    public void addChannelCookie(Object key, Object value) {
	channelCookies.put(key, value);
    }

    public void addEventStationCookie(Object key, Object value) {
	eventStationCookies.put(key, value);
    }

    public void addSeismogramCookie(Object key, Object value) {
	seismogramCookies.put(key, value);
    }


    void setEventCookies(Map m) {
	this.eventCookies = m;
    }

    void setStationCookies(Map m) {
	this.stationCookies = m;
    }

    void setChannelCookies(Map m) {
	this.channelCookies = m;
    }

    void setEventStationCookies(Map m) {
	this.eventStationCookies = m;
    }

    void setSeismogramCookies(Map m) {
	this.seismogramCookies = m;
    }

    private Map eventCookies;
    private Map stationCookies;
    private Map channelCookies;
    private Map eventStationCookies;
    private Map seismogramCookies;

}// CookieJar

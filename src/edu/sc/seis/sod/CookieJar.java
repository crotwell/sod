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

    /**
     * Creates a new <code>CookieJar</code> instance.
     *
     */
    public CookieJar (){
	eventCookies = new HashMap();
	stationCookies = new HashMap();
	channelCookies = new HashMap();
	eventStationCookies = new HashMap();
	seismogramCookies = new HashMap();
    }

    /**
     * Describe <code>getEventCookie</code> method here.
     *
     * @param key an <code>Object</code> value
     * @return an <code>Object</code> value
     */
    public Object getEventCookie(Object key) {
	return eventCookies.get(key);
    }

    /**
     * Describe <code>getStationCookie</code> method here.
     *
     * @param key an <code>Object</code> value
     * @return an <code>Object</code> value
     */
    public Object getStationCookie(Object key) {
	return stationCookies.get(key);
    }

    /**
     * Describe <code>getChannelCookie</code> method here.
     *
     * @param key an <code>Object</code> value
     * @return an <code>Object</code> value
     */
    public Object getChannelCookie(Object key) {
	return channelCookies.get(key);
    }

    /**
     * Describe <code>getEventStationCookie</code> method here.
     *
     * @param key an <code>Object</code> value
     * @return an <code>Object</code> value
     */
    public Object getEventStationCookie(Object key) {
	return eventStationCookies.get(key);
    }

    /**
     * Describe <code>getSeismogramCookie</code> method here.
     *
     * @param key an <code>Object</code> value
     * @return an <code>Object</code> value
     */
    public Object getSeismogramCookie(Object key) {
	return seismogramCookies.get(key);
    }

    /**
     * Describe <code>addEventCookie</code> method here.
     *
     * @param key an <code>Object</code> value
     * @param value an <code>Object</code> value
     */
    public void addEventCookie(Object key, Object value) {
	eventCookies.put(key, value);
    }

    /**
     * Describe <code>addStationCookie</code> method here.
     *
     * @param key an <code>Object</code> value
     * @param value an <code>Object</code> value
     */
    public void addStationCookie(Object key, Object value) {
	stationCookies.put(key, value);
    }

    /**
     * Describe <code>addChannelCookie</code> method here.
     *
     * @param key an <code>Object</code> value
     * @param value an <code>Object</code> value
     */
    public void addChannelCookie(Object key, Object value) {
	channelCookies.put(key, value);
    }

    /**
     * Describe <code>addEventStationCookie</code> method here.
     *
     * @param key an <code>Object</code> value
     * @param value an <code>Object</code> value
     */
    public void addEventStationCookie(Object key, Object value) {
	eventStationCookies.put(key, value);
    }

    /**
     * Describe <code>addSeismogramCookie</code> method here.
     *
     * @param key an <code>Object</code> value
     * @param value an <code>Object</code> value
     */
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

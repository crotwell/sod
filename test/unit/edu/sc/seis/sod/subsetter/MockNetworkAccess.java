/**
 * MyNetworkAccess.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.SamplingRange;
import edu.iris.Fissures.IfNetwork.OrientationRange;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.Calibration;
import edu.iris.Fissures.NotImplemented;
import edu.iris.Fissures.IfNetwork.TimeCorrection;
import edu.iris.Fissures.IfNetwork.ChannelIdIterHolder;
import edu.iris.Fissures.Area;
import edu.iris.Fissures.AuditElement;

public class MockNetworkAccess implements NetworkAccess{
    
    private NetworkAttr attributes;
    
    public MockNetworkAccess(NetworkAttr attributes){
        this.attributes = attributes;
    }
    
    /**
     * Method retrieve_stations
     *
     * @return   a Station[]
     *
     */
    public Station[] retrieve_stations() {
        return null;
    }
    
    /**
     * Method retrieve_instrumentation
     *
     * @param    p1                  a  ChannelId
     * @param    p2                  a  Time
     *
     * @return   an Instrumentation
     *
     * @exception   ChannelNotFound
     *
     */
    public Instrumentation retrieve_instrumentation(ChannelId p1, Time p2) throws ChannelNotFound {
        return null;
    }
    
    /**
     * Method retrieve_channels_by_code
     *
     * @param    p1                  a  String
     * @param    p2                  a  String
     * @param    p3                  a  String
     *
     * @return   a Channel[]
     *
     * @exception   ChannelNotFound
     *
     */
    public Channel[] retrieve_channels_by_code(String p1, String p2, String p3) throws ChannelNotFound {
        return null;
    }
    
    /**
     * Method retrieve_channel
     *
     * @param    p1                  a  ChannelId
     *
     * @return   a Channel
     *
     * @exception   ChannelNotFound
     *
     */
    public Channel retrieve_channel(ChannelId p1) throws ChannelNotFound {
        return null;
    }
    
    /**
     * Method retrieve_for_station
     *
     * @param    p1                  a  StationId
     *
     * @return   a Channel[]
     *
     */
    public Channel[] retrieve_for_station(StationId p1) {
        return null;
    }
    
    /**
     * Method get_audit_trail
     *
     * @return   an AuditElement[]
     *
     * @exception   NotImplemented
     *
     */
    public AuditElement[] get_audit_trail() throws NotImplemented {
        return null;
    }
    
    /**
     * Method retrieve_calibrations
     *
     * @param    p1                  a  ChannelId
     * @param    p2                  a  TimeRange
     *
     * @return   a Calibration[]
     *
     * @exception   ChannelNotFound
     * @exception   NotImplemented
     *
     */
    public Calibration[] retrieve_calibrations(ChannelId p1, TimeRange p2) throws ChannelNotFound, NotImplemented {
        return null;
    }
    
    /**
     * Method get_audit_trail_for_channel
     *
     * @param    p1                  a  ChannelId
     *
     * @return   an AuditElement[]
     *
     * @exception   ChannelNotFound
     * @exception   NotImplemented
     *
     */
    public AuditElement[] get_audit_trail_for_channel(ChannelId p1) throws ChannelNotFound, NotImplemented {
        return null;
    }
    
    /**
     * Method retrieve_grouping
     *
     * @param    p1                  a  ChannelId
     *
     * @return   a ChannelId[]
     *
     * @exception   ChannelNotFound
     *
     */
    public ChannelId[] retrieve_grouping(ChannelId p1) throws ChannelNotFound {
        return null;
    }
    
    /**
     * Method retrieve_all_channels
     *
     * @param    p1                  an int
     * @param    p2                  a  ChannelIdIterHolder
     *
     * @return   a ChannelId[]
     *
     */
    public ChannelId[] retrieve_all_channels(int p1, ChannelIdIterHolder p2) {
        return null;
    }
    
    /**
     * Method retrieve_time_corrections
     *
     * @param    p1                  a  ChannelId
     * @param    p2                  a  TimeRange
     *
     * @return   a TimeCorrection[]
     *
     * @exception   ChannelNotFound
     * @exception   NotImplemented
     *
     */
    public TimeCorrection[] retrieve_time_corrections(ChannelId p1, TimeRange p2) throws ChannelNotFound, NotImplemented {
        return null;
    }
    
    /**
     * Method locate_channels
     *
     * @param    p1                  an Area
     * @param    p2                  a  SamplingRange
     * @param    p3                  an OrientationRange
     *
     * @return   a Channel[]
     *
     */
    public Channel[] locate_channels(Area p1, SamplingRange p2, OrientationRange p3) {
        return null;
    }
    
    /**
     * Method retrieve_groupings
     *
     * @return   a ChannelId[][]
     *
     */
    public ChannelId[][] retrieve_groupings() {
        return null;
    }
    
    /**
     * Method get_attributes
     *
     * @return   a NetworkAttr
     *
     */
    public NetworkAttr get_attributes() {
        return attributes;
    }
    

    

}


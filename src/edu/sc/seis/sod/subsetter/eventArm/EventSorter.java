/**
 * EventSorter.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.database.event.StatefulEvent;
import edu.sc.seis.sod.database.event.JDBCEventStatus;
import java.sql.SQLException;
import org.w3c.dom.Element;

public class EventSorter{
    public EventSorter(){ this(null); }

    public EventSorter(Element config){
        try {
            evStatus = new JDBCEventStatus();
        } catch (SQLException e) {
            CommonAccess.handleException(e, "Trouble creating JDBCEventStatus for sorting events");
        }
        setSorting(config);
    }

    public void setSorting(Element config){
        if(config != null  && config.getChildNodes().getLength() != 0){
            Element sortType = (Element)config.getFirstChild();
            if(sortType.getNodeName().equals("time")){
                query = "SELECT DISTINCT origineventid, origin_time FROM origin ORDER BY origin_time";
            }else if(sortType.getNodeName().equals("magnitude")){
                query = "SELECT DISTINCT origineventid, magnitudevalue FROM origin, magnitude " +
                    "WHERE origin.originid IN (SELECT originid FROM eventaccess) and " +
                    "magnitudevalue IN (SELECT MAX(magnitudevalue) FROM magnitude WHERE origin.originid = magnitude.originid) " +
                    "ORDER BY  magnitudevalue";
            }else if(sortType.getNodeName().equals("depth")){
                query = "SELECT DISTINCT origineventid, locationdepthvalue FROM origin, location " +
                    "WHERE originid IN (SELECT originid FROM eventaccess) and " +
                    "locationdepthvalue IN (SELECT locationdepthvalue FROM location WHERE originlocationid = locationid) " +
                    "ORDER BY  locationdepthvalue";
            }
            if(query != null){
                String ordering = sortType.getAttribute("order");
                if(ordering.equals("descending")) query += " DESC";
                else query += " ASC";
            }
        }
    }

    public StatefulEvent[] getSortedEvents() throws SQLException{
        if(query == null) return evStatus.getAll();
        return evStatus.get(query, "origineventid");
    }

    private String query;

    private JDBCEventStatus evStatus;
}

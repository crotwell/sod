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
                query = "SELECT DISTINCT origin_event_id, time_stamp FROM origin, time WHERE origin_time_id = time_id ORDER BY time_stamp";
            }else if(sortType.getNodeName().equals("magnitude")){
                query = "SELECT DISTINCT origin_event_id, magnitudevalue FROM origin, magnitude " +
                    "WHERE origin_id IN (SELECT origin_id FROM eventaccess) and " +
                    "magnitudevalue IN (SELECT MAX(magnitudevalue) FROM magnitude WHERE origin_id = originid) " +
                    "ORDER BY  magnitudevalue";
            }else if(sortType.getNodeName().equals("depth")){
                query = "SELECT DISTINCT origin_event_id, quantity_value  FROM origin, quantity " +
                    "WHERE origin_id IN (SELECT origin_id FROM eventaccess) and " +
                    "quantity_value IN (SELECT quantity_value FROM quantity WHERE quantity_id IN (SELECT loc_depth_id FROM location WHERE origin_location_id = loc_id)) " +
                    "ORDER BY  quantity_value";
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
        return evStatus.get(query, "origin_event_id");
    }

    private String query;

    private JDBCEventStatus evStatus;
}

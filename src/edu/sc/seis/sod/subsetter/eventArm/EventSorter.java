/**
 * EventSorter.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.event.JDBCEventStatus;
import edu.sc.seis.sod.database.event.StatefulEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;

public class EventSorter{
    public EventSorter(){ this(null); }

    public EventSorter(Element config){
        try {
            evStatus = new JDBCEventStatus();
        } catch (SQLException e) {
            GlobalExceptionHandler.handle("Trouble creating JDBCEventStatus for sorting events", e);
        }
        setSorting(config);
    }

    private String makeExtraClause(List statii){
        String extraClause = " and eventcondition IN (";
        Iterator it = statii.iterator();
        boolean first = true;
        while(it.hasNext()){
            if(first){ first = false; }
            else{ extraClause += ", "; }
            extraClause += " " + ((Status)it.next()).getAsShort();
        }
        extraClause += ") and eventid = origin_event_id ";
        return extraClause;
    }

    public void setSorting(Element config){
        if(config != null  && config.getChildNodes().getLength() != 0){
            Element sortType = (Element)config.getFirstChild();
            String extraClause = "";
            if(config.getElementsByTagName("status").getLength() > 0){
                List statii = new ArrayList();
                Element status = (Element)config.getElementsByTagName("status").item(0);
                if(SodUtil.getNestedText(status).equals("SUCCESS")){
                    statii.add( Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                           Standing.SUCCESS));
                    statii.add(Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                          Standing.IN_PROG));

                }else if(SodUtil.getNestedText(status).equals("FAILED")){
                    statii.add(Status.get(Stage.EVENT_ORIGIN_SUBSETTER,
                                          Standing.REJECT));
                }else if(SodUtil.getNestedText(status).equals("IN PROGRESS")){
                    statii.add(Status.get(Stage.EVENT_ORIGIN_SUBSETTER,
                                          Standing.IN_PROG));
                }
                extraClause = makeExtraClause(statii);
            }
            if(sortType.getNodeName().equals("time")){
                query = "SELECT DISTINCT origin_event_id, time_stamp, eventcondition FROM origin, time, eventstatus " +
                    "WHERE origin_time_id = time_id" + extraClause +
                    " ORDER BY time_stamp";
            }else if(sortType.getNodeName().equals("magnitude")){
                query = "SELECT DISTINCT origin_event_id, magnitudevalue, eventcondition FROM origin, magnitude, eventstatus " +
                    "WHERE origin_id IN (SELECT origin_id FROM eventaccess) and " +
                    "magnitudevalue IN (SELECT MAX(magnitudevalue) FROM magnitude WHERE origin_id = originid) " +
                    extraClause +
                    " ORDER BY  magnitudevalue";
            }else if(sortType.getNodeName().equals("depth")){
                query = "SELECT DISTINCT origin_event_id, quantity_value, eventcondition  FROM origin, quantity, eventstatus " +
                    "WHERE origin_id IN (SELECT origin_id FROM eventaccess) and " +
                    "quantity_value IN (SELECT quantity_value FROM quantity WHERE quantity_id IN (SELECT loc_depth_id FROM location WHERE origin_location_id = loc_id)) " +
                    extraClause +
                    " ORDER BY  quantity_value";
            }
            if(query != null){
                String ordering = sortType.getAttribute("order");
                if(ordering.equals("descending")) query += " DESC";
                else query += " ASC";
            }
        }
    }

    public synchronized StatefulEvent[] getSortedEvents() throws SQLException{
        if(query == null) return evStatus.getAll();
        return evStatus.get(query, "origin_event_id");
    }

    private String query;

    private JDBCEventStatus evStatus;
}

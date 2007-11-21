/**
 * EventSorter.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.origin;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.hibernate.StatefulEventDB;

public class EventSorter {
	public EventSorter() {
		this(null);
	}

	public EventSorter(Element config) {
		try {
			evStatus = new StatefulEventDB();
			setSorting(config);
		} catch (SQLException e) {
			GlobalExceptionHandler.handle(
					"Trouble creating JDBCEventStatus for sorting events", e);
		}
	}

	protected String makeExtraClause(Element status) {
		List statusList = new ArrayList();
		if (SodUtil.getNestedText(status).equals("SUCCESS")) {
			statusList.add(Status.get(Stage.EVENT_CHANNEL_POPULATION,
					Standing.SUCCESS));
			statusList.add(Status.get(Stage.EVENT_CHANNEL_POPULATION,
					Standing.IN_PROG));

		} else if (SodUtil.getNestedText(status).equals("FAILED")) {
			statusList.add(Status
					.get(Stage.EVENT_ORIGIN_SUBSETTER, Standing.REJECT));
		} else if (SodUtil.getNestedText(status).equals("IN PROGRESS")) {
			statusList.add(Status.get(Stage.EVENT_ORIGIN_SUBSETTER,
					Standing.IN_PROG));
		}
		String extraClause = "(";
		Iterator it = statusList.iterator();
		boolean first = true;
		while (it.hasNext()) {
			if (first) {
				first = false;
			} else {
				extraClause += ", ";
			}
			extraClause += " " + ((Status) it.next()).getAsShort();
		}
		extraClause += ") ";
		return extraClause;
	}

	public void setSorting(Element config) throws SQLException {
		if (config != null && config.getChildNodes().getLength() != 0) {
			Element sortType = (Element) config.getFirstChild();
			if (config.getElementsByTagName("status").getLength() > 0) {
				statii = makeExtraClause((Element) config.getElementsByTagName(
						"status").item(0));
			}
			sort = sortType.getNodeName();

			String ordering = sortType.getAttribute("order");
			if (ordering.equals("descending")) {
				ascending = false;
			} else {
				ascending = true;
			}

		}
	}

	public List getSortedEvents() {
		if (prep == null) {
			return evStatus.getAll();
		}
		return evStatus.get(statii, sort, ascending);
	}

	private boolean ascending = true;

	private String sort = "time";

	private String statii = "";

	private PreparedStatement prep;

	private StatefulEventDB evStatus;
}

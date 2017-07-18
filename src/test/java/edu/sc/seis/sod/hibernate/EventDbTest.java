package edu.sc.seis.sod.hibernate;

import static org.junit.Assert.*;

import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.mock.event.MockEventAccessOperations;
import edu.sc.seis.sod.model.event.CacheEvent;


public class EventDbTest {

    @Before
    public void setUp() throws Exception {
        BasicConfigurator.configure();
        Properties props = new Properties();
        props.put("hibernate.connection.url", "jdbc:hsqldb:mem:.");
        assertNotNull("Start class", Start.class);
        assertNotNull("Start default value", "/"+Start.DEFAULT_PROPS);
        assertNotNull("props as stream: "+Start.DEFAULT_PROPS, Start.class.getResourceAsStream("/"+Start.DEFAULT_PROPS));
        props.load(Start.class.getResourceAsStream("/"+Start.DEFAULT_PROPS));
        HibernateUtil.setUp(props);
    }

    @Test
    public void testGetEvent() {
        fail("Not yet implemented");
    }

    @Test
    public void testPut() {
        CacheEvent event = MockEventAccessOperations.createEvent();
        EventDB.getSingleton().put(event);
    }
}

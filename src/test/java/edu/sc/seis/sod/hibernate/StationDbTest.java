package edu.sc.seis.sod.hibernate;

import static org.junit.Assert.*;

import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;

import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.sod.Start;


public class StationDbTest {

    @Before
    public void setUp() throws Exception {
        BasicConfigurator.configure();
        Properties props = new Properties();
        props.put("hibernate.connection.url", "jdbc:hsqldb:mem:.");
        props.put("hibernate.cache.use_second_level_cache", "false");
        props.put("hibernate.hbm2ddl.auto", "create");
        assertNotNull("Start class", Start.class);
        assertNotNull("Start default value", "/"+Start.DEFAULT_PROPS);
        assertNotNull("props as stream: "+Start.DEFAULT_PROPS, Start.class.getResourceAsStream("/"+Start.DEFAULT_PROPS));
        //props.load(Start.class.getResourceAsStream("/"+Start.DEFAULT_PROPS));
        HibernateUtil.setUp(props);
    }

    @Test
    public void test() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Network net = new Network();
        net.setCode("XX");
        net.setDescription("Testing");
        net.setStartDate("19921201T000000.000000Z");
        Transaction t = session.beginTransaction();
        session.persist(net);
        session.close();
        assertTrue("dbid > 0", net.getDbid() > 0);
    }
}

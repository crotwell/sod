package edu.sc.seis.sod.hibernate;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.mock.station.MockChannel;
import edu.sc.seis.sod.mock.station.MockNetworkAttr;
import edu.sc.seis.sod.mock.station.MockStation;


public class StationDbTest {

    public static void setUpDB() throws Exception {
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



    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        StationDbTest.setUpDB();
    }

    @Test
    public void test() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Network net = new Network();
        net.setCode("XX");
        net.setDescription("Testing");
        net.setStartDate("19921201T00:00:00.000000Z");
        Transaction t = session.beginTransaction();
        session.persist(net);
        session.close();
        assertTrue("dbid > 0", net.getDbid() > 0);
    }
    
    public void testPutNet() {
        NetworkDB ndb = NetworkDB.getSingleton();
        ndb.put(MockNetworkAttr.createNetworkAttr());
        ndb.commit();
        List<Network> netList = ndb.getAllNetworks();
        assertTrue("net size", netList.size() > 0);
    }
    
    public void testPutStation() {
        Station s = MockStation.createStation();
        NetworkDB ndb = NetworkDB.getSingleton();
        ndb.put(s.getNetwork());
        ndb.put(s);
        ndb.commit();
        List<Network> netList = ndb.getAllNetworks();
        assertTrue("net size", netList.size() > 0);
        List<Station> staList = ndb.getStationForNet(s.getNetwork());
        assertTrue("sta size", staList.size() > 0);
    }
    
    public void testPutChannel() throws NotFound {
        NetworkDB ndb = NetworkDB.getSingleton();
        Channel c = MockChannel.createChannel();
        ndb.put(c.getNetwork());
        ndb.put(c.getStation());
        ndb.put(c);
        ndb.commit();
        Channel dbChan = ndb.getChannel(c.getNetworkCode(), c.getStationCode(), c.getLocCode(), c.getChannelCode(), c.getStartDateTime());
        assertNotNull("channel", dbChan);
    }
}

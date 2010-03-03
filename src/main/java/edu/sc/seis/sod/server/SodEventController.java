package edu.sc.seis.sod.server;

import java.util.Properties;

import org.omg.CORBA.ORB;

import edu.sc.seis.cormorant.event.EventController;
import edu.sc.seis.cormorant.event.EventDataAccess;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.hibernate.HibernateUtil;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEventDB;


public class SodEventController extends EventController {

    public SodEventController(Properties confProps, String serverPropName, ORB orb) throws Exception {
        super(confProps, serverPropName, orb);
    }

    
    protected EventDataAccess createEventDataAccess(ORB orb, Properties props) {
        ConnMgr.installDbProperties(props, new String[0]);
        synchronized(HibernateUtil.class) {
            HibernateUtil.setUpFromConnMgr(props, getClass().getResource("/edu/sc/seis/sod/data/ehcache.xml"));
            SodDB.configHibernate(HibernateUtil.getConfiguration());
        }
        return new StatefulEventDBDataAccess(StatefulEventDB.getSingleton());
    }    
}

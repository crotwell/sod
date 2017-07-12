package edu.sc.seis.sod.hibernate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import net.sf.ehcache.CacheManager;

public class HibernateUtil {
    
    private static String configFile = "edu/sc/seis/fissuresUtil/hibernate/hibernate.cfg.xml";

    private static SessionFactory sessionFactory;

    private static Configuration configuration;

    public synchronized static SessionFactory getSessionFactory() {
        if(sessionFactory == null) {
            logger.debug("Sessionfactory is null, creating...");
            sessionFactory = getConfiguration().buildSessionFactory();
        }
        return sessionFactory;
    }

    public synchronized static Configuration getConfiguration() {
        if(configuration == null) {
            logger.debug("Hibernate configuration is null, loading config from "
                    + configFile);
            configuration = new Configuration().configure(configFile);
        }
        return configuration;
    }

    public synchronized static void setConfigFile(String s) {
        logger.warn("Reseting hibernate configuration: " + s);
        configFile = s;
        sessionFactory = null;
        configuration = null;
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HibernateUtil.class);

    public static void setUpFromConnMgr(Properties props, URL ehcacheConfig) {
        if ( ! props.containsKey("ehcache.disk.store.dir")) {
            String dirname = "hibernate_ehcache";
            File f;
            try {
                f = File.createTempFile(dirname, ".cache");
                f.delete();
                if (f.mkdir()) {
                    dirname = f.getCanonicalPath();
                } else {
                    dirname = dirname+"_"+Math.random();
                }
            } catch(IOException e) {
                // oops
                dirname = dirname+"_"+Math.random();
            }
            props.put("ehcache.disk.store.dir", dirname);
        }
    	setUpEHCache(ehcacheConfig);
        setUpFromConnMgr(props);
    }
    
    static void setUpEHCache(URL ehcacheConfig) {
        if (ehcacheConfig == null) {throw new IllegalArgumentException("ehcacheConfig cannot be null");}
        // configure EhCache
        try {
            CacheManager singletonManager = CacheManager.create(ehcacheConfig.openStream());
        } catch(IOException e) {
            throw new RuntimeException("Trouble finding EhCache config from "+ehcacheConfig.toString(), e);
        }
    }
    
    public static void setUpFromConnMgr(Properties props) {
        String dialect;
        if(ConnMgr.getDB_TYPE().equals(ConnMgr.HSQL)) {
            logger.info("using hsql dialect");
            dialect = org.hibernate.dialect.HSQLDialect.class.getName();
        } else if(ConnMgr.getDB_TYPE().equals(ConnMgr.POSTGRES)) {
            logger.info("using postgres dialect");
            dialect = org.hibernate.dialect.PostgreSQLDialect.class.getName();
        } else if(ConnMgr.getDB_TYPE().equals(ConnMgr.EDB)) {
            logger.info("using postgresql as edb dialect");
            dialect = org.hibernate.dialect.PostgresPlusDialect.class.getName();
        } else if(ConnMgr.getDB_TYPE().equals(ConnMgr.MYSQL)) {
            logger.info("using mysql dialect");
            dialect = org.hibernate.dialect.MySQLDialect.class.getName();
        } else if(ConnMgr.getDB_TYPE().equals(ConnMgr.ORACLE)) {
            logger.info("using oracle dialect");
            dialect = org.hibernate.dialect.Oracle10gDialect.class.getName();
        } else {
            throw new RuntimeException("Unknown database type: '"+ConnMgr.getDB_TYPE()+"'");
        }
        setUp(dialect, ConnMgr.getDriver(), ConnMgr.getURL(), ConnMgr.getUser(), ConnMgr.getPass(), props);
        getConfiguration().addProperties(ConnMgr.getDBProps());
    }
    
    
    public static void setUp(String dialect, String driverClass, String dbURL, String username, String password, Properties props) {
        logger.info("setup: "+dialect+" "+driverClass+"  "+dbURL+"  "+username+"  "+password);
        synchronized(HibernateUtil.class) {
            getConfiguration().setProperty("hibernate.dialect", dialect);
            getConfiguration().setProperty("hibernate.connection.driver_class",
                                           driverClass)
                    .setProperty("hibernate.connection.url", dbURL)
                    .setProperty("hibernate.connection.username",
                                 username)
                    .setProperty("hibernate.connection.password",
                                 password)
                    .addProperties(props);
        }
    }
    
    public static String getDialectForURL(String url) {
        if (url.startsWith("jdbc:hsql")) {
            return org.hibernate.dialect.HSQLDialect.class.getName();
        } else if (url.startsWith("jdbc:postgresql")) {
            return org.hibernate.dialect.PostgreSQLDialect.class.getName();
        } else if (url.startsWith("jdbc:edb")) {
            return org.hibernate.dialect.PostgresPlusDialect.class.getName();
        } else if (url.startsWith("jdbc:mysql")) {
            return org.hibernate.dialect.MySQL5Dialect.class.getName();
        } else if (url.startsWith("jdbc:oracle")) {
            return org.hibernate.dialect.Oracle10gDialect.class.getName();
        }
        throw new RuntimeException("Unable to determine database dialect from URL: "+url);
    }
    
    public static final URL DEFAULT_EHCACHE_CONFIG = HibernateUtil.class.getClassLoader().getResource("edu/sc/seis/fissuresUtil/hibernate/ehcache.xml");

}

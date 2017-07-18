package edu.sc.seis.sod.hibernate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.service.ServiceRegistry;

import net.sf.ehcache.CacheManager;

public class HibernateUtil {

/* definitions are in edu/sc/seis/sod/hibernate/*.hbm.xml
 * loaded from hibernate.cgf.xml
*/
    
    private static String configFile = "edu/sc/seis/sod/hibernate/hibernate.cfg.xml";

    private static SessionFactory sessionFactory;

    public synchronized static SessionFactory getSessionFactory() {
        if(sessionFactory == null) {
            logger.debug("Sessionfactory is null, creating...");
            

            ServiceRegistry standardRegistry = getServiceRegistryBuilder().build();

            MetadataSources sources = new MetadataSources( standardRegistry );
            
            
            
            MetadataBuilder metadataBuilder = sources.getMetadataBuilder();
            
            addDateFunctions(metadataBuilder);
            
            Metadata metadata = metadataBuilder.build();
            sessionFactory = metadata.getSessionFactoryBuilder().build();
        }
        return sessionFactory;
    }
    
    private static void addDateFunctions(MetadataBuilder metadataBuilder) {
        String dbUrl = props.getProperty("hibernate.connection.url");
        if(dbUrl.startsWith("jdbc:hsql")) {
            metadataBuilder.applySqlFunction("datediff",
                                  new SQLFunctionTemplate(org.hibernate.type.StandardBasicTypes.LONG,
                                                          "datediff(?1, ?2, ?3)"));
            metadataBuilder.applySqlFunction("milliseconds_between",
                                  new SQLFunctionTemplate(org.hibernate.type.StandardBasicTypes.LONG,
                                                          "datediff('ms', ?1, ?2)"));
            metadataBuilder.applySqlFunction("seconds_between",
                                  new SQLFunctionTemplate(org.hibernate.type.StandardBasicTypes.LONG,
                                                          "datediff('ss', ?1, ?2)"));
        } else if(dbUrl.startsWith("jdbc:postgresql")) {
            metadataBuilder.applySqlFunction("milliseconds_between",
                                  new SQLFunctionTemplate(org.hibernate.type.StandardBasicTypes.LONG,
                                                          "extract(epoch from (?2 - ?1)) * 1000"));
            metadataBuilder.applySqlFunction("seconds_between",
                                  new SQLFunctionTemplate(org.hibernate.type.StandardBasicTypes.LONG,
                                                          "extract(epoch from (?2 - ?1))"));
        }
    }

    static StandardServiceRegistryBuilder standardServiceRegistryBuilder;
    
    public static StandardServiceRegistryBuilder getServiceRegistryBuilder() {
        if (standardServiceRegistryBuilder == null) {
            standardServiceRegistryBuilder = new StandardServiceRegistryBuilder()
                .configure( configFile);
        }
        return standardServiceRegistryBuilder;
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HibernateUtil.class);

    public static void setUp(Properties props, URL ehcacheConfig) {
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
        setUp(props);
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
    
    
    public static void setUp(Properties inProps) {
        synchronized(HibernateUtil.class) {
            props = inProps;
            getServiceRegistryBuilder()
                    .applySettings(props);
        }
    }
    
    private static Properties props;
    
    public static final URL DEFAULT_EHCACHE_CONFIG = HibernateUtil.class.getClassLoader().getResource("edu/sc/seis/sod/data/ehcache.xml");

}

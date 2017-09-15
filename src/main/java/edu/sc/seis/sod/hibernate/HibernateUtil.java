package edu.sc.seis.sod.hibernate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import javax.xml.stream.XMLEventReader;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.registry.selector.spi.StrategySelector;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.hbm2ddl.TargetTypeHelper;
import org.hibernate.tool.schema.TargetType;

import net.sf.ehcache.CacheManager;

public class HibernateUtil {
    
    public synchronized static SessionFactory getSessionFactory() {
        if(sessionFactory == null) {
            throw new RuntimeException("Sessionfactory is null, HibernateUtil.setUp must be called first.");
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
        System.err.println("ToDo: set up ehcache or another...");
//    	setUpEHCache(ehcacheConfig);
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
            StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(props)
                    .build();

            MetadataBuilder metadataBuilder = new MetadataSources( standardRegistry )
                    .addResource( "edu/sc/seis/sod/hibernate/StationXML.hbm.xml" )
                    .addResource( "edu/sc/seis/sod/hibernate/Common.hbm.xml" )
                    .addResource( "edu/sc/seis/sod/hibernate/Event.hbm.xml" )
                    .addResource( "edu/sc/seis/sod/hibernate/sod.hbm.xml" )
                    .getMetadataBuilder();
            addDateFunctions(metadataBuilder);
            metadata = metadataBuilder.build();

            SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();

//          Add a custom observer
            sessionFactoryBuilder.addSessionFactoryObservers( new CustomSessionFactoryObserver() );

            sessionFactory = sessionFactoryBuilder.build();
            deploySchema(true);
        }
    }
    
    public static void deploySchema(boolean haltOnError) {

        EnumSet<TargetType> targetTypes = TargetTypeHelper.parseCommandLineOptions( "script,database" ); // "stdout,script,database"
        
        SchemaUpdate update = new SchemaUpdate()
        .setOutputFile( "sod_hibernate.out" )
        .setHaltOnError(haltOnError)
        .setDelimiter( null );
        update.execute( targetTypes, metadata );
        List<Throwable> exceptions = update.getExceptions();
        for (Throwable t : exceptions) {
            logger.warn("problem update schema", t);
        }

        if (haltOnError && exceptions.size() >0) {
            Throwable first = exceptions.get(0);
            if (first instanceof Exception) {
                throw new RuntimeException("Problem updating schema", first);
            } else {
                throw (RuntimeException)first;
            }
        }
    }
    
    private static Metadata metadata;
    
    private static SessionFactory sessionFactory;
    
    private static Properties props;
    
    public static final URL DEFAULT_EHCACHE_CONFIG = HibernateUtil.class.getClassLoader().getResource("edu/sc/seis/sod/data/ehcache.xml");

}

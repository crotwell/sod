package edu.sc.seis.sod.hibernate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;


public class ConnMgr {

    /**
     * Use this method to add loacations for sql property files to be stored
     * When setDB is loaded, if there is a default.props file at that location,
     * it will be added to the existing properties, and if one of the <TYPE OF
     * DB>.props exists, it will also be loaded.
     */
    public static void addPropsLocation(String loc) {
        synchronized(propLocs) {
            if(!propLocs.contains(loc))
                propLocs.add(loc);
            if(props != null) {
                try {
                    load(loc, props);
                } catch(IOException e) {
                    throw new RuntimeException("Bad props location " + loc, e);
                }
            }
        }
    }

    /**
     * Sets the ConnMgr to use the default db, which as of now is an in-memory
     * HSQLDb
     */
    public static void setDB() throws IOException {
        setDB(DB_NAME);
    }

    /**
     * Sets the DB to be used based on the default values for the name. Names
     * fissuresUtil knows are ConnMgr.MCKOI, ConnMgr.HSQL, and ConnMgr.POSTGRES
     * 
     * @throws IOException
     *             if some of the props don't load
     */
    public static void setDB(String dbName) throws IOException {
        DB_NAME = dbName;
        Properties props = new Properties();
        synchronized(propLocs) {
            Iterator it = propLocs.iterator();
            while(it.hasNext())
                load((String)it.next(), props);
        }
        setDB(props);
    }

    private static void load(String loc, Properties existing)
            throws IOException {
        ClassLoader cl = ConnMgr.class.getClassLoader();
        load(cl, loc + DEFAULT_PROPS, existing);
        if(DB_NAME == HSQL)
            load(cl, loc + HSQL_PROPS, existing);
        else if(DB_NAME == MCKOI)
            load(cl, loc + MCKOI_PROPS, existing);
        else if(DB_NAME == POSTGRES)
            load(cl, loc + POSTGRES_PROPS, existing);
        else if(DB_NAME == EDB)
            load(cl, loc + EDB_PROPS, existing);
        else if(DB_NAME == MYSQL)
            load(cl, loc + MYSQL_PROPS, existing);
        else if(DB_NAME == ORACLE)
            load(cl, loc + ORACLE_PROPS, existing);
    }

    private static void load(ClassLoader cl, String loc, Properties existing)
            throws IOException {
        InputStream in = cl.getResourceAsStream(loc);
        if(in != null)
            existing.load(in);
    }

    public static void setDB(Properties newprops) {
        props = newprops;
    }

    public static boolean hasSQL(String key) {
        return getProps().containsKey(key);
    }

    public static String getSQL(String key) {
        String SQL = getProps().getProperty(key);
        if(SQL == null) {
            throw new IllegalArgumentException("No such sql entry "
                    + key
                    + " Make sure the properties files are in the jars and are being loaded");
        }
        return SQL;
    }

    /** @returns the classname of the jdbc driver. */
    public static String getDriver() {
        return getProps().getProperty("driver");
    }
    
    /** @returns the database product, ie hsql, postgres, etc. */
    public static String getDB_TYPE() {
        return DB_NAME;
    }

    public static void setURL(String url) {
        if (url == null || url.length() == 0) {
            throw new RuntimeException("URL is empty.");
        }
        if (!firstConnection) {
            throw new RuntimeException("Attemp to set database url a second time: "+url);
        }
        try {
        ConnMgr.url = url;
        if (url.startsWith("jdbc:hsql")) {
            setDB(HSQL);
        } else if (url.startsWith("jdbc:postgresql")) {
            setDB(POSTGRES);
        } else if (url.startsWith("jdbc:edb")) {
            setDB(EDB);
        } else if (url.startsWith("jdbc:mysql")) {
            setDB(MYSQL);
            String[] splitURL = url.split("\\?");
            if (splitURL.length != 1) {
                String[] URLparams = splitURL[1].split("&");
                for (String param : URLparams) {
                    if (param.startsWith("user=")) {
                        getProps().setProperty("user", param.substring("user=".length()));
                    }
                    if (param.startsWith("password=")) {
                        getProps().setProperty("password", param.substring("password=".length()));
                    }
                }
            }
        } else if (url.startsWith("jdbc:oracle")) {
            setDB(ORACLE);
            String[] splitURL;
            if (url.indexOf("thin") > 0){
               splitURL = (url.split("@//"))[0].split("thin:");
            }
            else if (url.indexOf("oci") > 0){
                if (url.indexOf("@//") > 0) {
                    splitURL = (url.split("@//"))[0].split("oci:");
                } else {
                    splitURL = (url.split("@"))[0].split("oci:");
                }
            } else {
                splitURL = new String[0];
            }

            if (splitURL.length > 1) {
                String[] URLparams = splitURL[1].split("/");
                if (URLparams.length != 1) {
                   getProps().setProperty("user", URLparams[0]);
                   getProps().setProperty("password", URLparams[1]);
                }
            }
        }
            checkDriver();
        } catch (Exception e) {
            throw new RuntimeException("Unable to load driver: "+getDriver(), e);
        }

    }
    public static void setURL(String url,
                              String databaseUser,
                              String databasePassword) {
        setURL(url);
        if(databaseUser != null) {
            getProps().setProperty("user", databaseUser);
        }
        if(databasePassword != null) {
            getProps().setProperty("password", databasePassword);
        }
    }

    public static String getURL() {
        if(url == null) {
            url = getProps().getProperty("URL");
        }
        return url;
    }

    public static String getPass() {
        return getProps().getProperty("password");
    }

    public static String getUser() {
        return getProps().getProperty("user");
    }

    private static Properties getProps() {
        synchronized(ConnMgr.class) {
            if(props == null) {
                try {
                    setDB();
                } catch(IOException e) {}
            }
        }
        return props;
    }
    
    public static Properties getDBProps() {
        return dbProperties;
    }
    
    static void checkDriver() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
            String driver = getDriver();
            if(firstTime) {
                logger.debug("Using " + driver + " on " + getURL()+" for user "+getUser());
                lastDriverForConnection = driver;
                firstTime = false;
            }
            if(!driver.equals(lastDriverForConnection)) {
                logger.warn("Previous connections were created with "
                        + lastDriverForConnection + " but now " + driver
                        + " is being used");
                lastDriverForConnection = driver;
            }
            Class.forName(getDriver()).newInstance();
            }

    public static Connection createConnection() throws SQLException {
        try {
            checkDriver();
        } catch (Exception e) {
            SQLException sql = new SQLException("Cannot create driver: "+getDriver());
            sql.initCause(e);
            throw sql;
        }
        Connection conn = DriverManager.getConnection(getURL(),
                                                      getUser(),
                                                      getPass());
        if(firstConnection
                && getURL().startsWith("jdbc:hsql")
                && conn.getMetaData()
                        .getDatabaseProductVersion()
                        .compareTo("1.8.0") >= 0) {
            Statement stmt = conn.createStatement();
            try {
                stmt.execute("SET PROPERTY \"hsqldb.default_table_type\" 'CACHED'");
                stmt.execute("CHECKPOINT");
            } catch(SQLException e) {
                logger.debug("Unable to set default table type to CACHED", e);
            }
            firstConnection = false;
        }
        return conn;
    }

    private static boolean firstConnection = true;

    private static String lastDriverForConnection;

    private static boolean firstTime = true;

    public static void installDbProperties(Properties sysProperties,
                                           Properties dbProperties) {
        ConnMgr.dbProperties = dbProperties;
        if(dbProperties.containsKey(DB_SERVER_PORT)) {
            if(dbProperties.containsKey(DBURL_KEY)) {
                logger.error("-hsql properties and SOD properties are both specifying the db connection.  Using -hsql properties");
            }
            // Use hsqldb properties specified in
            // http://hsqldb.sourceforge.net/doc/guide/ch04.html
            String url = "jdbc:hsqldb:hsql://localhost";
            if(dbProperties.containsKey(DB_SERVER_PORT)) {
                url += ":" + dbProperties.getProperty(DB_SERVER_PORT);
            }
            url += "/";
            if(dbProperties.containsKey("server.dbname.0")) {
                url += dbProperties.getProperty("server.dbname.0");
            }
            logger.debug("Setting db url from "+DB_SERVER_PORT+" to " + url);
            setURL(url);
        } else if(dbProperties.containsKey(DBURL_KEY)) {
            logger.debug("Setting db url from "+DBURL_KEY+" to "
                         + dbProperties.getProperty(DBURL_KEY));
            setURL(dbProperties.getProperty(DBURL_KEY));
        } else if(sysProperties.containsKey(DBURL_KEY)) {
            logger.debug("Setting db url from "+DBURL_KEY+" in sys props to "
                    + sysProperties.getProperty(DBURL_KEY));
            setURL(sysProperties.getProperty(DBURL_KEY));
        } else {
            logger.debug("using default url of " + getURL());
        }
        if (dbProperties.containsKey(DBUSER_KEY)) {
            logger.debug("Setting db user from "+DBUSER_KEY+" (db) to " + dbProperties.get(DBUSER_KEY));
            getProps().put("user", dbProperties.get(DBUSER_KEY));
        } else if (sysProperties.containsKey(DBUSER_KEY)) {
            logger.debug("Setting db user from "+DBUSER_KEY+" (sys) to " + sysProperties.get(DBUSER_KEY));
            getProps().put("user", sysProperties.get(DBUSER_KEY));
        }
        if (dbProperties.containsKey(DBPASSWORD_KEY)) {
            getProps().put("password", dbProperties.get(DBPASSWORD_KEY));
        } else {
            getProps().put("password", "");
        }
    }

    public static Properties readDbProperties(String[] args) {
        Properties dbProperties = new Properties();
        boolean loadedFromArg = false;
        for(int i = 0; i < args.length - 1; i++) {
            if(args[i].equals("-hsql")) {
                System.out.println("Loading db props from "+args[i+1]);
                try {
                    Initializer.loadProps(new FileInputStream(args[i + 1]),
                                          dbProperties);
                } catch(FileNotFoundException e) {
                    logger.error("Unable to find file " + args[i + 1]
                            + " specified by -hsql");
                } catch(IOException e) {
                    logger.error("Error reading " + args[i + 1]
                            + " specified by -hsql", e);
                }
                loadedFromArg = true;
            }
        }
        if(!loadedFromArg) {
            try {
                logger.debug("No -hsql argument found, trying to load from server.properties in current working directory");
                Initializer.loadProps(new FileInputStream("server.properties"),
                                      dbProperties);
                logger.debug("loaded props from server.properties in working directory");
            } catch(FileNotFoundException e) {
                logger.debug("Didn't find default server.properties file");
            } catch(IOException e) {
                logger.error("Error reading default server.properties file", e);
            }
        }
        return dbProperties;
    }

    public static void installDbProperties(Properties sysProperties,
                                           String[] args) {
        installDbProperties(sysProperties, readDbProperties(args));
    }
    
    public static boolean checkDatabaseConn()  throws SQLException {
        logger.info("URL: "+ConnMgr.getURL());
        logger.info("Type: "+ConnMgr.getDB_TYPE());
        logger.info("Driver: "+ConnMgr.getDriver());
        logger.info("User: "+ConnMgr.getUser());
        //logger.info("Password: "+ConnMgr.getPass());
        logger.info("Trying connection");
        Connection conn = ConnMgr.createConnection();
        logger.info("Connection ok, database version: "+conn.getMetaData().getDatabaseProductVersion());
        conn.close();
        return true;
    }
    
    public static void main(String[] args) throws SQLException {
        BasicConfigurator.configure();
        String url = args[0];
        logger.info("setting URL: "+url);
        ConnMgr.setURL(url);
        checkDatabaseConn();
    }

    private static final String DEFAULT_LOC = "edu/sc/seis/fissuresUtil/database/props/";

    public static final String DEFAULT = "default";

    private static String DEFAULT_PROPS = "default.props";

    public static final String HSQL = "HSQL";

    private static String HSQL_PROPS = "HSQL.props";

    public static final String MCKOI = "MCKOI";

    private static String MCKOI_PROPS = "MCKOI.props";
    
    public static final String MYSQL = "MYSQL";
    
    public static final String MYSQL_PROPS = "Mysql.props";

    public static final String ORACLE = "ORACLE";
    
    public static final String ORACLE_PROPS = "Oracle.props";

    public static final String POSTGRES = "POSTGRES";

    public static final String POSTGRES_PROPS = "Postgres.props";
    
    public static final String EDB = "EDB";

    public static final String EDB_PROPS = "Edb.props";

    public static final String DB_SERVER_PORT = "server.port";

    public static final String DBURL_KEY = "fissuresUtil.database.url";
    public static final String DBUSER_KEY = "fissuresUtil.database.user";
    public static final String DBPASSWORD_KEY = "fissuresUtil.database.password";

    private static String DB_NAME = HSQL;

    private static Properties props, dbProperties = new Properties();

    private static List propLocs = new ArrayList();

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConnMgr.class);

    private static String url;
    static {
        propLocs.add(DEFAULT_LOC);
    }
}// ConnMgr

package edu.sc.seis.sod.hibernate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public abstract class Initializer {


    // DESPAIR at the VERY THOUGHT of stumping APOP(Advanced Prop Option Parser)
    // Actually, despair that you have reached this juncture where 7 equivalent
    // options are accepted
    public static final String[] POSSIBLE_PROP_OPTION_NAMES = new String[] {"-p",
                                                                            "-props",
                                                                            "-prop",
                                                                            "-properties",
                                                                            "--prop",
                                                                            "--props",
                                                                            "--properties"};

    public static Properties loadProperties(String[] args) throws IOException {
        return loadProperties(args, System.getProperties());
    }

    public static Properties loadProperties(String[] args, Properties baseProps)
            throws IOException {
        return loadProperties(args, baseProps, true);
    }

    /**
     * If chatty is true, notify std out for every prop file loaded
     */
    public static Properties loadProperties(String[] args, Properties baseProps, boolean chatty)
            throws IOException {
        for(int i = 0; i < args.length - 1; i++) {
            for(int j = 0; j < POSSIBLE_PROP_OPTION_NAMES.length; j++) {
                String propFilename = args[i + 1];
                if(args[i].equals(POSSIBLE_PROP_OPTION_NAMES[j])) {
                    // override with values in local directory,
                    // but still load defaults with original name
                    loadProps(new FileInputStream(propFilename), baseProps);
                    if(chatty){
                        System.out.println("loaded props file from " + args[i + 1]);
                    }
                }
            }
        }
        return baseProps;
    }

    public static Properties getProps() {
        return props;
    }

    public static void loadProps(InputStream propStream, Properties baseProps)
            throws IOException {
        baseProps.load(propStream);
        propStream.close();
    }

    private static Properties props;

    private static Object initLock = new Object();

    private static final String[] EMPTY_ARGS = {};

    private static Logger logger = LoggerFactory.getLogger(Initializer.class);

}

package edu.sc.seis.sod.subsetter.network;

import java.util.regex.Pattern;
import edu.iris.Fissures.IfNetwork.NetworkAttr;

/**
 * @author groves Created on May 4, 2005
 */
public class TemporaryNetwork implements NetworkSubsetter {

    public boolean accept(NetworkAttr attr) {
        String code = attr.get_code();
        if(isTemporary(code)) {
            System.out.println("FOUND TEMP " + code);
        }
        return isTemporary(code);
    }

    public static boolean isTemporary(String code) {
        return p.matcher(code).matches();
    }

    private static Pattern p = Pattern.compile("X|Y|Z");
}

/**
 * SodJDBC.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.database;

import edu.sc.seis.fissuresUtil.database.ConnMgr;
import java.io.IOException;

public class SodJDBC{
    static{
        ConnMgr.addPropsLocation("edu/sc/seis/sod/database/props/");
        try {
            ConnMgr.setDB();
        } catch (IOException e) { throw new RuntimeException("Some props weren't found!");}
    }
}


/**
 * NetworkArmContext.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.status.networkArm;

import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.database.NetworkDbObject;
import edu.sc.seis.sod.database.network.JDBCNetworkUnifier;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.context.Context;

public class NetworkArmContext  extends AbstractContext {

    public NetworkArmContext() throws SQLException {
        super();
        jdbcNetwork = new JDBCNetworkUnifier();
    }

    public NetworkArmContext(Context context) throws SQLException {
        super(context);
        jdbcNetwork = new JDBCNetworkUnifier();
    }

    public Object internalGet(String key) {
        if (key.equals(ALL_NETS_KEY)) {
            try {
                NetworkDbObject[] netdbs = jdbcNetwork.getAllNets(Start.getNetworkArm().getNetworkDC());
                ArrayList out = new ArrayList(netdbs.length);
                for (int i = 0; i < netdbs.length; i++) {
                    out.add(netdbs[i]);
                }
                return out;
            } catch (NotFound e) {
                throw new RuntimeException("can't get for key="+key, e);
            } catch (SQLException e) {
                throw new RuntimeException("can't get for key="+key, e);
            } catch (NetworkNotFound e) {
                throw new RuntimeException("can't get for key="+key, e);
            }
        } else if (key.equals(SUCCESSFUL_NETS_KEY)) {
            try {
                NetworkDbObject[] netdbs = Start.getNetworkArm().getSuccessfulNetworks();
                ArrayList out = new ArrayList(netdbs.length);
                for (int i = 0; i < netdbs.length; i++) {
                    out.add(netdbs[i]);
                }
                return out;
            } catch (Exception e) {
                throw new RuntimeException("can't get for key="+key, e);
            }
        } else if (key.length() == 2 &&  ! (key.startsWith("X") || key.startsWith("Y") || key.startsWith("Z"))) {
            // try as a network code
            try{
                NetworkDbObject[] netdbs = jdbcNetwork.getAllNets(Start.getNetworkArm().getNetworkDC());
                ArrayList out = new ArrayList(netdbs.length);
                for (int i = 0; i < netdbs.length; i++) {
                    if (netdbs[i].getNetworkAccess().get_attributes().get_code().equals(key)) {
                        return netdbs[i];
                    }
                }
            } catch (NotFound e) {
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("can't get for key="+key, e);
            } catch (NetworkNotFound e) {
                return null;
            }
        }
        //else
        return null;
    }

    public boolean internalContainsKey(Object key) {
        if (key.equals(ALL_NETS_KEY)) {
            return true;
        } else {
            return false;
        }
    }

    public Object[] internalGetKeys() {
        return new String[] {ALL_NETS_KEY};
    }

    public Object internalRemove(Object key) {
        throw new RuntimeException("Read only context, operation remove not permitted: key="+key);
    }

    public Object internalPut(String key, Object p2) {
        throw new RuntimeException("Read only context, operation put not permitted: key="+key);
    }

    protected JDBCNetworkUnifier jdbcNetwork;

    public static final String ALL_NETS_KEY = "network_arm_all";

    public static final String SUCCESSFUL_NETS_KEY = "network_arm_successful";

    public static final String NET_PREFIX = "network_arm_";
}


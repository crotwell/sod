package edu.sc.seis.sod.subsetter.network;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

public class NetworkCode implements NetworkSubsetter {

    public NetworkCode(Element config) {
        this.desiredCode = SodUtil.getText(config);
        Matcher m = TEMP_NET.matcher(desiredCode);
        if(m.matches()) {
            desiredCode = m.group(1);
            year = m.group(2);
        }
    }

    public StringTree accept(NetworkAttr attr) throws Exception {
        if(attr.get_code().equals(desiredCode)
                && (year == null || year.equals(NetworkIdUtil.getTwoCharYear(attr.get_id())))) {
            return new Pass(this);
        }
        return new Fail(this);
    }

    public String getCode() {
        return desiredCode;
    }

    private String desiredCode;

    private String year;

    private static final Pattern TEMP_NET = Pattern.compile("([XYZ][A-Z])(\\d{2})");
}
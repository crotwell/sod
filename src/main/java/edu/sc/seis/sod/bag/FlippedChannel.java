package edu.sc.seis.sod.bag;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InstrumentSensitivity;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.Orientation;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

public class FlippedChannel {
    public static ChannelSeismogram correct(Channel chan,
                                            LocalSeismogramImpl seis) throws FissuresException{
        return correct(chan, seis, null);
    }

    public static ChannelSeismogram correct(Channel chan,
                                            LocalSeismogramImpl seis,
                                            InstrumentSensitivity sens)
            throws FissuresException {
        if(check(chan)) {
            return new ChannelSeismogram(OrientationUtil.flip(chan),
                                         Arithmatic.mul(seis, -1),
                                         null);
        }
        return new ChannelSeismogram(chan, seis, sens);
    }

    public static boolean check(Channel chan) {
        return (chan.getCode().charAt(2) == 'Z' && check(OrientationUtil.getUp(),
                                                          chan))
                || (chan.getCode().charAt(2) == 'N' && check(OrientationUtil.getNorth(),
                                                              chan))
                || (chan.getCode().charAt(2) == 'E' && check(OrientationUtil.getEast(),
                                                              chan));
    }

    public static boolean check(Orientation correct, Channel chan) {
        return OrientationUtil.angleBetween(correct, Orientation.of(chan)) >= 180 - tol;
    }

    private static double tol = 0.01;
}

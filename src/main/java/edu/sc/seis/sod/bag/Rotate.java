package edu.sc.seis.sod.bag;

import java.awt.geom.AffineTransform;

import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.Orientation;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.station.ChannelId;

/**
 * Rotate.java Created: Sun Dec 15 13:43:21 2002
 * 
 * @author Philip Crotwell
 * @version $Id: Rotate.java 22072 2011-02-18 15:43:18Z crotwell $
 */
public class Rotate  {

    public static boolean areRotatable(Orientation xOrient, Orientation yOrient) {
        return areRotatable(xOrient, yOrient, NINTY_DEGREE_TOLERANCE);
    }

    public static boolean areRotatable(Orientation xOrient, Orientation yOrient, float toleranceDegrees) {
        // want y north, x east, or at least x + 90 deg == y
        double angle = (xOrient.azimuth - yOrient.azimuth) % 360;
        if (angle < 0) {angle += 360;}
        if (Math.abs(angle - 90) < toleranceDegrees 
                || Math.abs(angle - 270) < toleranceDegrees) {
            return true;
        }
        return false;
    }

    public static LocalSeismogramImpl[] rotateGCP(LocalSeismogramImpl x,
                                                  Orientation xOrient,
                                                  LocalSeismogramImpl y,
                                                  Orientation yOrient,
                                                  Location staLoc,
                                                  Location evtLoc,
                                                  String transverseCode,
                                                  String radialCode)
            throws FissuresException, IncompatibleSeismograms {
        return rotateGCP(x, xOrient, y, yOrient, staLoc, evtLoc, transverseCode, radialCode, NINTY_DEGREE_TOLERANCE);
    }

    public static LocalSeismogramImpl[] rotateGCP(LocalSeismogramImpl x,
                                                  Orientation xOrient,
                                                  LocalSeismogramImpl y,
                                                  Orientation yOrient,
                                                  Location staLoc,
                                                  Location evtLoc,
                                                  String transverseCode,
                                                  String radialCode,
                                                  float toleranceDegrees)
            throws FissuresException, IncompatibleSeismograms {
        // want y north, x east, or at least x + 90 deg == y
        double angle = (xOrient.azimuth - yOrient.azimuth) % 360;
        if (angle < 0) {angle += 360;}
        if (Math.abs(angle - 90) < toleranceDegrees) {
            // ok
        } else if (Math.abs(angle - 270) < toleranceDegrees) {
            // need to swap
            LocalSeismogramImpl tmp = x;
            Orientation tmpOrient = xOrient;
            x = y;
            xOrient = yOrient;
            y = tmp;
            yOrient = tmpOrient;
        } else {
            throw new IncompatibleSeismograms("not 90 deg separation: "+xOrient.azimuth+" - "+yOrient.azimuth+" = "+angle+" < tol of "+toleranceDegrees);
        }
        double radialAz = getRadialAzimuth(staLoc, evtLoc);
        float[][] data = Rotate.rotate(x, y, dtor(radialAz-yOrient.azimuth));
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[2];

        ChannelId xChanId = replaceChannelOrientation(y.channel_id, transverseCode);
        out[0] = new LocalSeismogramImpl(x.get_id() + "Transverse",
                                         x.begin_time,
                                         data[0].length,
                                         x.sampling_info,
                                         x.y_unit,
                                         xChanId,
                                         data[0]);
        ChannelId yChanId = replaceChannelOrientation(y.channel_id, radialCode);
        out[1] = new LocalSeismogramImpl(y.get_id() + "Radial",
                                         y.begin_time,
                                         data[1].length,
                                         y.sampling_info,
                                         y.y_unit,
                                         yChanId,
                                         data[1]);
        return out;
    }
    
    public static ChannelId replaceChannelOrientation(ChannelId chanId, String orientation) {
        return ChannelGroup.replaceChannelOrientation(chanId, orientation);
    }

    /**
     * rotates the two seismograms to the great circle path transverse (X) and
     * radial (Y). It is assumed that the two seismograms orientation are
     * perpendicular to each other and are oriented in the east, x and north, y,
     * directions. They must also have the same number of data points.
     * 
     * @throws FissuresException
     * @throws IncompatibleSeismograms
     * @return the rotated data from the two seismograms, index 0 is the
     *          tangential and index 1 is the radial.
     */
    public static float[][] rotateGCP(LocalSeismogramImpl x,
                                      LocalSeismogramImpl y,
                                      Location staLoc,
                                      Location evtLoc)
            throws FissuresException, IncompatibleSeismograms {
        return Rotate.rotate(x, y, dtor(getRadialAzimuth(staLoc, evtLoc)));
    }
    
    public static double getRadialAzimuth(Location staLoc,
                                             Location evtLoc) {
        DistAz distAz = new DistAz(staLoc, evtLoc);
        return (180 + distAz.getBaz()) % 360;
    }
    
    public static double getTransverseAzimuth(Location staLoc,
                                             Location evtLoc) {
        DistAz distAz = new DistAz(staLoc, evtLoc);
        return (270 + distAz.getBaz()) % 360;
    }
    
    /**
     * rotates the two seismograms by the given angle. It is assumed that the
     * two seismograms orientation are perpendicular to each other and that the
     * sense of the rotation is from x towards y.
     * 
     * @throws IncompatibleSeismograms
     * @return the rotated data from the two seismograms, index 0 is the new x
     *          and index 1 is the new y.
     */
    public static float[][] rotate(LocalSeismogramImpl x,
                                   LocalSeismogramImpl y,
                                   double radians) throws FissuresException,
            IncompatibleSeismograms {
        float[][] data = new float[2][];
        float[] temp = x.get_as_floats();
        data[0] = new float[temp.length];
        System.arraycopy(temp, 0, data[0], 0, temp.length);
        temp = y.get_as_floats();
        data[1] = new float[temp.length];
        System.arraycopy(temp, 0, data[1], 0, temp.length);
        rotate(data[0], data[1], radians);
        return data;
    }

    /**
     * Rotates the x and y arrays by the given angle in radians. The x and y
     * axis are assumed to be perpendicular. Theta, in radians, is positive from
     * x towards y, and so a rotation of PI/2 puts x into y and -y into x.
     * 
     * @throws IncompatibleSeismograms
     */
    public static void rotate(float[] x, float[] y, double radians)
            throws IncompatibleSeismograms {
        rotate(x, y, AffineTransform.getRotateInstance(radians));
    }

    /**
     * Performs the rotation from the given matrix. It is assumed to be a pure
     * rotation matrix (no translation) and the translation components of the
     * affine transform are ignored if present.
     * 
     * @throws IncompatibleSeismograms
     */
    public static void rotate(float[] x, float[] y, AffineTransform affine)
            throws IncompatibleSeismograms {
        if(x.length != y.length) { throw new IncompatibleSeismograms("x and y must have the same length. "
                + x.length + " " + y.length); }
        float tempx, tempy;
        // matrix is m00 m10 m01 m11 where the matrix is
        //           m00 m01
        //           m10 m11
        double[] matrix = new double[4];
        affine.getMatrix(matrix);
        for(int i = 0; i < x.length; i++) {
            tempx = x[i];
            tempy = y[i];
            x[i] = (float)(tempx * matrix[0] + tempy * matrix[2]);
            y[i] = (float)(tempx * matrix[1] + tempy * matrix[3]);
        }
    }

    public static void rotate(float[] x,
                              float[] y,
                              float[] z,
                              double theta,
                              double phi) {
        throw new RuntimeException("Not implemented yet");
    }

    public static double dtor(double degree) {
        return Math.PI * degree / 180.0;
    }

    public static double rtod(double radian) {
        return radian * 180.0 / Math.PI;
    }
    
    public static final float NINTY_DEGREE_TOLERANCE = .01f;
} // Rotate

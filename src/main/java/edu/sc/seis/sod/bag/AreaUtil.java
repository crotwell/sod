/**
 * AreaUtil.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.bag;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.model.common.Area;
import edu.sc.seis.sod.model.common.BoxAreaImpl;
import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.GlobalAreaImpl;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.LocationType;
import edu.sc.seis.sod.model.common.PointDistanceAreaImpl;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.UnitImpl;

public class AreaUtil {

    public static BoxAreaImpl makeContainingBox(Area a) {
        if(a instanceof BoxAreaImpl) {
            return (BoxAreaImpl)a;
        } else if(a instanceof GlobalAreaImpl) {
            return new BoxAreaImpl(-90, 90, -180, 180);
        } else if(a instanceof PointDistanceAreaImpl) {
            PointDistanceAreaImpl pda = (PointDistanceAreaImpl)a;
            float maxDegree = (float)distanceToDegrees(pda.max_distance);
            if (maxDegree >= 180) {return new BoxAreaImpl(-90, 90, -180, 180);}
            float maxLong = wrapLong((float)SphericalCoords.lonFor(pda.latitude, pda.longitude, maxDegree,  90));
            float minLong = wrapLong((float)SphericalCoords.lonFor(pda.latitude, pda.longitude, maxDegree, -90));
            float minLat = pda.latitude - maxDegree;
            if (minLat < -90) {minLat = -90;}
            float maxLat = pda.latitude + maxDegree;
            if (maxLat > 90) {maxLat = 90;}
            return new BoxAreaImpl(minLat, maxLat, minLong, maxLong);
        }
        throw new RuntimeException("Unknown Area: " + a.getClass().getName());
    }

    private static float wrapLong(float lon) {
        if(lon > 180) {
            return -180 + (lon - 180);
        } else if(lon < -180) {
            lon = 180 + (lon + 180);
        }
        return lon;
    }

    public static List<Channel> inArea(Area area, List<Channel> channels) {
        List<Channel> out = new ArrayList<Channel>();
        for(Channel chan : channels) {
            if(inArea(area, Location.of(chan))) {
                out.add(chan);
            }
        }
        return out;
    }

    public static boolean inArea(Area area, Location point) {
        return inArea(area, point.latitude, point.longitude);
    }
        
    public static boolean inArea(Area area, double latitude, double longitude) {
        if(area instanceof GlobalAreaImpl) {
            return true;
        } else if(area instanceof BoxAreaImpl) {
            return inBox((BoxAreaImpl)area, latitude, longitude);
        } else if(area instanceof PointDistanceAreaImpl) {
            return inDonut((PointDistanceAreaImpl)area, latitude, longitude);
        }
        throw new RuntimeException("Unknown Area type: "
                + area.getClass().getName());
    }

    private static boolean inDonut(PointDistanceAreaImpl a, double latitude, double longitude) {
        DistAz distAz = new DistAz(a.latitude,
                                   a.longitude,
                                   latitude,
                                   longitude);
        double minDegree = distanceToDegrees(a.min_distance);
        double maxDegree = distanceToDegrees(a.max_distance);
        return (distAz.getDelta() >= minDegree && distAz.getDelta() <= maxDegree);
    }

    private static double distanceToDegrees(QuantityImpl minDist) {
        if(((UnitImpl)minDist.getUnit()).isConvertableTo(UnitImpl.DEGREE)) {
            return ((QuantityImpl)minDist).getValue(UnitImpl.DEGREE);
        }
        return DistAz.kilometersToDegrees(((QuantityImpl)minDist).getValue(UnitImpl.KILOMETER));
    }

    private static boolean inBox(BoxAreaImpl box, double latitude, double longitude) {
        return (latitude >= box.min_latitude
                && latitude <= box.max_latitude
                && longitude % 360 >= box.min_longitude % 360 && longitude % 360 <= box.max_longitude % 360);
    }

    public static boolean inArea(Location[] bounds, Location point) {
        float lonA, latA, lonB, latB;
        int inside = 0;
        for(int i = 0; i < bounds.length; i++) {
            lonA = bounds[i].longitude - point.longitude;
            latA = bounds[i].latitude - point.latitude;
            lonB = bounds[(i + 1) % bounds.length].longitude - point.longitude;
            latB = bounds[(i + 1) % bounds.length].latitude - point.latitude;
            int check = polygonPointCheck(lonA, latA, lonB, latB);
            if(check == 4) {
                return true;
            }
            inside += check;
        }
        return (inside != 0);
    }

    private static int polygonPointCheck(float lonA,
                                         float latA,
                                         float lonB,
                                         float latB) {
        if(latA * latB > 0) {
            return 0;
        }
        if((lonA * latB != lonB * latA) || (lonA * lonB > 0)) {
            if(latA * latB < 0) {
                if(latA > 0) {
                    if(latA * lonB >= lonA * latB) {
                        return 0;
                    }
                    return -2;
                }
                if(lonA * latB >= latA * lonB) {
                    return 0;
                }
                return 2;
            }
            if(latB == 0) {
                if(latA == 0) {
                    return 0;
                } else if(lonB > 0) {
                    return 0;
                } else if(latA > 0) {
                    return -1;
                }
                return 1;
            } else if(lonA > 0) {
                return 0;
            } else if(latB > 0) {
                return 1;
            }
            return -1;
        }
        return 4;
    }

    public static Location[] loadPolygon(BufferedReader in) throws IOException {
        ArrayList<Location> out = new ArrayList<Location>();
        String line;
        while((line = in.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("#") || line.length() < 3) {
                continue;
            }
            StringTokenizer tokenizer = new StringTokenizer(line);
            float lon = BoxAreaImpl.sanitize(Float.parseFloat(tokenizer.nextToken()));
            float lat = Float.parseFloat(tokenizer.nextToken());
            out.add(new Location(lat, lon, null, null));
        }
        return (Location[])out.toArray(new Location[0]);
    }
}
package edu.sc.seis.sod.mock;

import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.model.common.LocationType;
import edu.sc.seis.sod.model.common.QuantityImpl;

public class MockLocation {

    public static Location create() {
        return create(0f, 0f);
    }

    public static Location createBerlin() {
        return create(52.31f, 13.24f, Defaults.TEN_K, Defaults.TEN_K);
    }

    /** just to avoid all those little "f"s, casts double to float. */
    public static Location create(double lat, double lon) {
        return create((float)lat, (float)lon);
    }
    
    public static Location create(float lat, float lon) {
        return create(lat, lon, Defaults.ZERO_K, Defaults.ZERO_K);
    }

    public static Location create(float lat,
                                  float lon,
                                  QuantityImpl depth,
                                  QuantityImpl elev) {
        return new Location(lat, lon, elev, depth);
    }

    public static Location[] create(int rows, int cols) {
        return create(rows, cols, -70, 70, -180, 180);
    }

    public static Location[] create(int rows,
                                    int cols,
                                    double minLat,
                                    double maxLat,
                                    double minLon,
                                    double maxLon) {
        Location[] locs = new Location[rows * cols];
        double lonStep = 0;
        if(cols > 1) {
            lonStep = (maxLon - minLon) / (cols - 1);
        }
        double latStep = 0;
        if(rows > 1) {
            latStep = (maxLat - minLat) / (rows - 1);
        }
        for(int i = 0; i < rows; i++) {
            double lat = minLat + latStep * i;
            int rowOffset = i * cols;
            double lon = minLon;
            for(int j = 0; j < cols; j++) {
                locs[rowOffset + j] = MockLocation.create((float)lat,
                                                          (float)lon);
                lon += lonStep;
            }
        }
        return locs;
    }

    public static Location[] createMultiple() {
        Location[] locs = new Location[3];
        locs[0] = create();
        locs[1] = createBerlin();
        locs[2] = create(21.3f, 31.4f, Defaults.ZERO_K, Defaults.TEN_K);
        return locs;
    }
}
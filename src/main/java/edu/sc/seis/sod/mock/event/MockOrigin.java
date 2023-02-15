package edu.sc.seis.sod.mock.event;

import java.time.Instant;

import edu.sc.seis.sod.mock.Defaults;
import edu.sc.seis.sod.mock.MockLocation;
import edu.sc.seis.sod.mock.MockParameterRef;
import edu.sc.seis.sod.model.event.Magnitude;
import edu.sc.seis.sod.model.event.OriginImpl;

public class MockOrigin {

    public static OriginImpl create() {
        return create(Defaults.EPOCH, MockMagnitude.createMagnitudes());
    }

    public static OriginImpl create(Instant time, float lat, float lon) {
        return new OriginImpl("latlon event",
                              "Test Data",
                              "Charlie Groves",
                              time,
                              MockLocation.create(lat, lon),
                              MockMagnitude.createMagnitudes(),
                              MockParameterRef.createParams());
    }

    public static OriginImpl create(Instant time, Magnitude[] mags) {
        return new OriginImpl("Epoch in Central Alaska",
                              "Test Data",
                              "Charlie Groves",
                              time,
                              MockLocation.create(),
                              mags,
                              MockParameterRef.createParams());
    }

    public static OriginImpl createWallFallOrigin() {
        return new OriginImpl("Fall of the Berlin Wall",
                              "Test Data",
                              "Charlie Groves",
                              Defaults.WALL_FALL,
                              MockLocation.createBerlin(),
                              MockMagnitude.createMagnitudes(),
                              MockParameterRef.createParams());
    }

    public static OriginImpl[] createOrigins() {
        OriginImpl[] origins = new OriginImpl[2];
        origins[0] = create();
        origins[1] = createWallFallOrigin();
        return origins;
    }

    public static OriginImpl createOrigin() {
        return create();
    }
    

    public static OriginImpl[] createOrigins(int num) {
        OriginImpl[] out = new OriginImpl[num];
        for(int i = 0; i < out.length; i++) {
            out[i] = create(Instant.now(), MockMagnitude.createMagnitudes());
        }
        return out;
    }
}

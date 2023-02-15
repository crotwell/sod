package edu.sc.seis.sod.mock.event;

import edu.sc.seis.sod.model.event.Magnitude;

public class MockMagnitude {

    public static Magnitude createMagnitude() {
        return getMagnitudes(4, 4, 1)[0][0];
    }

    public static final Magnitude[][] getMagnitudes(float minMag,
                                                    float maxMag,
                                                    int count) {
        Magnitude[][] magnitudes = new Magnitude[count][];
        float diff = (maxMag - minMag) / count;
        float curMag = minMag;
        for(int i = 0; i < count; i++, curMag += diff) {
            float curMag2 = curMag + 0.2f;
            float curMag3 = curMag - 0.3f;
            magnitudes[i] = new Magnitude[] {new Magnitude("type" + curMag,
                                                           curMag,
                                                           "contributor"
                                                                   + curMag),
                                             new Magnitude("type" + curMag2,
                                                           curMag2,
                                                           "contributor"
                                                                   + curMag2),
                                             new Magnitude("type" + curMag3,
                                                           curMag3,
                                                           "contributor"
                                                                   + curMag3)};
        }
        return magnitudes;
    }

    /**
     * @deprecated - use create methods instead of sharing fields in case
     *             someone changes the internals
     */
    public static final Magnitude[] MAGS = createMagnitudes();

    public static Magnitude[] createMagnitudes() {
        return new Magnitude[] {new Magnitude("type5", 5, "contributor5"),
                                new Magnitude("type4", 4, "contributor4"),
                                new Magnitude("type6", 6, "contributor6")};
    }
}

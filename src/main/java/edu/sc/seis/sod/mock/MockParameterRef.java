package edu.sc.seis.sod.mock;

import edu.sc.seis.sod.model.common.ParameterRef;

public class MockParameterRef {

    public static ParameterRef[] createParams() {
        return new ParameterRef[] {new ParameterRef("aid7", "creator7"),
                                   new ParameterRef("aid8", "creator8"),
                                   new ParameterRef("aid9", "creator9")};
    }

    /**
     * @deprecated - use create methods instead of sharing fields in case
     *             someone changes the internals
     */
    public static final ParameterRef[] params = createParams();
}

/**
 * List.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public class DataList extends AbstractMultigenitorForm {
    public DataList(int min, int max){ this(min, max, null); }

    public DataList(int min, int max, Form parent){ super(min, max, parent); }

    public FormProvider copyWithNewParent(Form newParent) {
        DataList l = new DataList(1, 1, newParent);
        copyKidsToNewParent(l);
        return l;
    }

}


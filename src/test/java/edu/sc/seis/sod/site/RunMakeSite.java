package edu.sc.seis.sod.site;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunMakeSite {

    @Test
    public void dumbTest() {
        double i=0;
        double j =Math.random();
        assertEquals(i, j, 2);
    }

    @Test
    public void makeSodSite() throws Exception {
        double i=0;
        double j =Math.random();
        assertEquals(i, j, 2);
        File buildSod = new File("build/velocity/sod");
        buildSod.mkdirs();

        MakeSite maker = new MakeSite();
        String[] args = new String[] {"--run-once", "--output-dir", "build/makesite", "-p", "site/sod.prop"};
        maker.main(args);
        assertEquals(3, args.length-1);
        assertTrue(false, "site created!");
    }
}

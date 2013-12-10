package com.turbospaces.protoc;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.turbospaces.protoc.gen.Generator;

@org.junit.Ignore
public class GeneratorTest {
    Generator g;

    @Before
    public void setup() {
        String curr = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File outDir = new File( curr + "../../target/generated-test-sources/protocgen" );
        outDir.mkdirs();
        g = new Generator( outDir, "example.protoc" );
    }

    @Test
    public void test() throws Exception {
        g.run();
    }
}

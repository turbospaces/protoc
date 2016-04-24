package com.turbospaces.protoc;

import java.io.File;

import org.junit.Test;

import com.turbospaces.protolang.Generator;

//@org.junit.Ignore
public class GeneratorTest {
    Generator g;

    @Test
    public void test() throws Exception {
        String curr = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File outDir = new File( curr + "../../target/generated-test-sources/protolang" );
        g = new Generator( outDir, new String[] { "example.lang" }, false );
        g.run();
    }
}

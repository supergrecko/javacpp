package org.bytedeco.javacpp.tools;

import org.bytedeco.javacpp.tools.presets.zlib;

import org.junit.Test;

import java.util.Properties;

public class ClangParserTest {
    @Test
    public void test() {
        ClangParser p = new ClangParser(null, new Properties(), zlib.class);

        assert true;
    }
}

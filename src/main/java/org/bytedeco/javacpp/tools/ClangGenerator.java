package org.bytedeco.javacpp.tools;

import org.bytedeco.llvm.clang.CXTranslationUnit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ClangGenerator {
    public ClangGenerator(Logger logger, Properties properties) {
        this.logger = logger;
        this.properties = properties;
    }

    final Logger logger;
    final Properties properties;
    final Map<String, CXTranslationUnit> translationUnits = new HashMap<>();
    InfoMap infoMap = null;

    public boolean generate(
        String sourceFilename,
        String jniConfigFilename,
        String reflectionConfigFilename,
        String headerFilename,
        String loadSuffix,
        String baseLoadSuffix,
        String classPath,
        Class<?>... classes) throws IOException {

        return false;
    }
}
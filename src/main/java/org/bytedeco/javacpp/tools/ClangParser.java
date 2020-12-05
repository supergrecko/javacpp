package org.bytedeco.javacpp.tools;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.ClassProperties;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.clang.CXIndex;
import org.bytedeco.llvm.clang.CXTranslationUnit;
import org.bytedeco.llvm.global.clang;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ClangParser {
    public ClangParser(Logger logger, Properties properties, Class cls) {
        this(logger, properties, defaultClangOptions, cls);
    }

    public ClangParser(Logger logger, Properties properties, int clangOptions, Class cls) {
        this.clangOptions = clangOptions;
        this.logger = logger;
        this.properties = properties;
        this.cls = cls;

        allProperties = Loader.loadProperties(cls, properties, true);
        clsProperties = Loader.loadProperties(cls, properties, false);

        List<Class> allInheritedClasses = allProperties.getInheritedClasses();

        for (Class clazz : allInheritedClasses) {
            try {
                InfoMapper infoMapper = ((InfoMapper)clazz.newInstance());
                if (infoMapper instanceof BuildEnabled) {
                    ((BuildEnabled)infoMapper).init(logger, properties, null);
                }
                infoMapper.map(infoMap);
            } catch (IllegalAccessException | ClassCastException | InstantiationException ignored) { }
        }

        try {
            InfoMapper infoMapper = ((InfoMapper)cls.newInstance());
            if (infoMapper instanceof BuildEnabled) {
                ((BuildEnabled)infoMapper).init(logger, properties, null);
            }
            infoMapper.map(leafInfoMap);
        } catch (IllegalAccessException | ClassCastException | InstantiationException ignored) { }

        infoMap.putAll(leafInfoMap);
    }

    /**
     * We're only caring about a single file at a time. This is fine because the parser
     * will copy all the headers over and forward declare missing types and macros
     */
    static final int defaultClangOptions
        = clang.CXTranslationUnit_SingleFileParse
        | clang.CXTranslationUnit_Incomplete
        | clang.CXTranslationUnit_SkipFunctionBodies
        | clang.CXTranslationUnit_KeepGoing;

    final Logger logger;
    final Properties properties;
    final InfoMap infoMap = new InfoMap();
    final InfoMap leafInfoMap = new InfoMap();
    final Class cls;

    final ClassProperties allProperties;
    final ClassProperties clsProperties;

    final int clangOptions;

    /** The language we wish to parse, either C header or C++ header */
    enum LanguageKind {
        C,
        CXX
    }

    public File[] parse(File outputDirectory, String[] classPath) throws IOException, ParserException {
        List<String> cHeaders = new ArrayList<>(allProperties.get("platform.cinlude"));
        List<String> cxxHeaders = new ArrayList<>(allProperties.get("platform.include"));

        List<String> allTargets = allProperties.get("target");
        List<String> allGlobals = allProperties.get("global");
        List<String> clsTargets = clsProperties.get("target");
        List<String> clsGlobals = clsProperties.get("global");
        List<String> clsHelpers = clsProperties.get("helper");

        String target = clsTargets.get(clsTargets.size() - 1);
        String global = clsGlobals.get(clsGlobals.size() - 1);

        return new File[0];
    }

    CXTranslationUnit createTranslationUnit(File headerFile, InfoMap infoMap, LanguageKind language) throws IOException, ParserException {
        CXTranslationUnit translationUnit = new CXTranslationUnit();
        CXIndex index = clang.clang_createIndex(0, 1);
        File file = headerFile.getAbsoluteFile();
        List<String> commandLineArgs = new ArrayList<>();

        if (!file.exists()) {
            throw new FileNotFoundException("Header file \"" + file.getAbsolutePath() + "\" does not exist");
        }

        if (language == LanguageKind.CXX) {
            commandLineArgs.add("-x");
            commandLineArgs.add("c++");
        }

        PointerPointer<BytePointer> args = new PointerPointer<>((String[])commandLineArgs.toArray());
        BytePointer fileName = new BytePointer(file.getAbsolutePath());
        int result = clang.clang_parseTranslationUnit2(index, fileName, args, commandLineArgs.size(), null, 0, clangOptions, translationUnit);

        if (result != 0) {
            throw new ParserException("Failed to create translation unit for file \"" + file.getAbsolutePath());
        }

        return translationUnit;
    }
}
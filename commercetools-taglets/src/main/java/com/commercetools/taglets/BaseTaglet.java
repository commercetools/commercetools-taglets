package com.commercetools.taglets;

import com.github.javaparser.ast.CompilationUnit;
import jdk.javadoc.doclet.Taglet;

import javax.lang.model.element.Element;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseTaglet implements Taglet {
    protected static final Map<String, File> classFiles = new HashMap<>();
    protected static final Map<File, CompilationUnit> parsedFiles = new HashMap<>();
    protected static List<File> directories;

    protected File findFile(String fullyQualifiedClassName, String partialFilePath, final Element element)
            throws IOException {
        final File cwd = allProjectsBase();
        boolean found = false;
        File result = null;
        if (directories == null) {
            directories = Files.walk(cwd.toPath())
                               .filter(Files::isDirectory)
                               .filter(path -> !path.startsWith("."))
                               .map(Path::toFile)
                               .collect(Collectors.toList());
        }
        for (final File directory : directories) {
            final List<String> possibleSubfolders = Arrays.asList("/src/test/java", "/src/it/java", "/src/integrationTest/java", "/src/main/java");
            for (int subIndex = 0; subIndex < possibleSubfolders.size(); subIndex++) {
                final String pathToTest = possibleSubfolders.get(subIndex) + "/" + partialFilePath;
                final File attempt = new File(directory, pathToTest).getCanonicalFile();
                if (attempt.exists() && !attempt.getPath().matches(".*\\/build\\/.*")) {
                    if (found) {
                        throw new RuntimeException(
                                String.format("the class %s exists multiple times.", fullyQualifiedClassName));
                    }
                    else {
                        result = attempt;
                        found = true;
                    }
                }
            }
        }
        if (!found) {
            throw new RuntimeException(
                    "cannot find file for " + fullyQualifiedClassName + " for " + element.getSimpleName() + " in " + cwd);
        }
        return result;
    }

    private File allProjectsBase() {
        return InternalTagletUtils.allProjectsBaseFile();
    }
}

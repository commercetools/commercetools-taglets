
package com.commercetools.build.taglets;

import static com.commercetools.build.taglets.InternalTagletUtils.usableException;
import static java.lang.String.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;

import jdk.javadoc.doclet.Taglet;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.sun.source.doctree.DocTree;

public final class CodeTaglet implements Taglet {

    private static List<File> directories;

    private static final Map<File, CompilationUnit> parsedFiles = new HashMap<>();

    @Override
    public Set<Taglet.Location> getAllowedLocations() {
        final Set<Location> allowedLocations = new HashSet<>();
        allowedLocations.add(Location.MODULE);
        allowedLocations.add(Location.PACKAGE);
        allowedLocations.add(Location.TYPE);
        allowedLocations.add(Location.CONSTRUCTOR);
        allowedLocations.add(Location.FIELD);
        allowedLocations.add(Location.METHOD);
        allowedLocations.add(Location.OVERVIEW);
        return allowedLocations;
    }

    public String toString(List<? extends DocTree> tags, Element element) {
        final DocTree docTree = tags.get(0);
        final int beginning = 2 + getName().length(); // { + @ + length of the tag name. Used to extract tag text (the part after @)
        final String text = docTree.toString().substring(beginning, docTree.toString().length() - 1).trim();
        try {
            return getString(text, element);
        }
        catch (Exception e) {
            throw usableException(this, text, element, e);
        }
    }

    private String getString(final String tagText, final Element element) throws IOException {
        try {
            int pos = tagText.indexOf("#");
            final boolean fullFileRequested = pos == -1;
            if (fullFileRequested) {
                pos = tagText.length();
            }
            final String fullyQualifiedClassName = tagText.substring(0, pos);
            final String partialFilePath = fullyQualifiedClassName.replace('.', '/').concat(".java");

            final File testFile = findFile(fullyQualifiedClassName, partialFilePath, element);

            String imports = "";
            String res = "";
            if (!parsedFiles.containsKey(testFile)) {
                parsedFiles.put(testFile, StaticJavaParser.parse(testFile));
            }

            if (fullFileRequested) {
                final CompilationUnit parse = parsedFiles.get(testFile);
                final ClassOrInterfaceDeclaration declaration = parse
                        .getTypes()
                        .stream().filter(typeDeclaration -> typeDeclaration instanceof ClassOrInterfaceDeclaration)
                        .map(typeDeclaration -> (ClassOrInterfaceDeclaration)typeDeclaration)
                        .filter(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getFullyQualifiedName().orElse("").endsWith(fullyQualifiedClassName))
                        .collect(Collectors.toList())
                        .get(0);
                res = declaration.getTokenRange().get().toString() + "\n";
                imports = parse.getImports().stream().map(Node::toString).collect(Collectors.joining("\n"));
            }
            else {
                String testName = tagText.substring(pos + 1).trim();
                final int posParenthesis = testName.indexOf("(");
                final String methodName = testName.substring(0, posParenthesis > 0 ? posParenthesis : testName.length())
                                                  .trim();
                final CompilationUnit parse = parsedFiles.get(testFile);
                final ClassOrInterfaceDeclaration declaration = parse
                        .getLocalDeclarationFromClassname(fullyQualifiedClassName)
                        .get(0);
                final MethodDeclaration method = declaration.getMethodsByName(methodName).get(0);
                final String bodyRange = Arrays
                        .stream(method.getBody().get().getTokenRange().get().toString().split("\n"))
                        .map(line -> line.replaceFirst("        ", ""))
                        .collect(Collectors.joining("\n"));
                res = bodyRange.substring(1, bodyRange.length() - 1).trim() + "\n";
            }
            final String htmlEscapedBody = htmlEscape(res);
            if ("".equals(htmlEscapedBody)) {
                throw new RuntimeException("Empty example for " + tagText + " in " + testFile.getAbsolutePath());
            }
            final String htmlEscapedImports = htmlEscape(imports);
            final String tagId = tagText.replaceAll("[^a-zA-Z0-9]", "-");
            final String absolutePath = testFile.getAbsolutePath();
            final String canonicalPath = new File(".").getAbsoluteFile().getCanonicalPath().replace("/build/docs", "");
            final String pathToGitHubTestFile = absolutePath.replace(canonicalPath,
                    "https://github.com/commercetools/commercetools-sdk-java-v2/blob/master");
            return "<div id=\"" + tagId + "%s\" class=code-example>" + (fullFileRequested
                    ? "<button type='button' style='display: none;' class='reveal-imports'>show/hide imports</button>"
                    + "<pre class='hide code-example-imports'><code class='java'>" + htmlEscapedImports
                    + "</code></pre>"
                    : "") + "<pre><code class='java'>" + htmlEscapedBody + "</code><p>See the <a href=\""
                    + pathToGitHubTestFile + "\" target=\"_blank\">test code</a>.</pre>" + "</div>";
        }
        catch (final Exception e) {
            System.err.println(e);
            System.err.println("in");
            System.err.println(element);
            throw e;
        }

    }

    private String htmlEscape(final String res) {
        return res.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private File findFile(String fullyQualifiedClassName, String partialFilePath, final Element element)
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
                                format("the class %s exists multiple times.", fullyQualifiedClassName));
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

    private List<String> fileToArray(File testFile) throws FileNotFoundException {
        final Scanner scanner = new Scanner(testFile);
        List<String> lines = new ArrayList<>();
        while (scanner.hasNext()) {
            lines.add(scanner.nextLine());
        }
        return lines;
    }

    public String getName() {
        return "include.example";
    }

    public boolean isInlineTag() {
        return true;
    }
}

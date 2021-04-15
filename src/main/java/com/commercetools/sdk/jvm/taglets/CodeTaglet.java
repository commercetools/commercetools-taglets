package com.commercetools.sdk.jvm.taglets;

import com.commercetools.build.taglets.InternalTagletUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.JavaUnit;
import org.jboss.forge.roaster.model.source.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static com.commercetools.build.taglets.InternalTagletUtils.usableException;
import static java.lang.String.format;

public final class CodeTaglet implements Taglet {

    /**
     * Generates the String output for a tag
     * @param tag
     * @return
     */
    public String toString(Tag tag) {
        try {
            return getString(tag);
        } catch (Exception e) {
            throw usableException(this, tag, e);
        }
    }

    private enum Position {
        START, IMPORTS, CODE
    }

    private String getString(final Tag tag) throws IOException {
        try {
            int pos = tag.text().indexOf("#");
            final boolean fullFileRequested = pos == -1;
            if (fullFileRequested) {
                pos = tag.text().length();
            }
            final String fullyQualifiedClassName = tag.text().substring(0, pos);
            final String partialFilePath = fullyQualifiedClassName.replace('.', '/').concat(".java");


            final File testFile = findFile(fullyQualifiedClassName, partialFilePath, tag);

            String imports = "";
            String res = "";
            if (fullFileRequested) {
                final CompilationUnit parse = StaticJavaParser.parse(testFile);
                final ClassOrInterfaceDeclaration declaration = parse.getLocalDeclarationFromClassname(fullyQualifiedClassName).get(0);
                res = declaration.getTokenRange().get().toString() + "\n";
                imports = parse.getImports().stream().map(Node::toString).collect(Collectors.joining("\n"));
            } else {
                String testName = tag.text().substring(pos + 1).trim();
                final String methodName = testName.substring(0, testName.indexOf("(")).trim();
                final CompilationUnit parse = StaticJavaParser.parse(testFile);
                final ClassOrInterfaceDeclaration declaration = parse.getLocalDeclarationFromClassname(fullyQualifiedClassName).get(0);
                final MethodDeclaration method = declaration.getMethodsByName(methodName).get(0);
                final String bodyRange = Arrays.stream(method.getBody().get().getTokenRange().get().toString().split("\n")).map(line -> line.replaceFirst("        ", "")).collect(Collectors.joining("\n"));
                res = bodyRange.substring(1, bodyRange.length() - 1 ).trim() + "\n";
            }
            final String htmlEscapedBody = htmlEscape(res);
            if ("".equals(htmlEscapedBody)) {
                throw new RuntimeException("Empty example for " + tag.text() + " in " + testFile.getAbsolutePath());
            }
            final String htmlEscapedImports = htmlEscape(imports);
            final String tagId = tag.text().replaceAll("[^a-zA-Z0-9]","-");
            final String absolutePath = testFile.getAbsolutePath();
            final String canonicalPath = new File(".").getAbsoluteFile().getCanonicalPath().replace("/target/site/apidocs", "");
            final String pathToGitHubTestFile = absolutePath.replace(canonicalPath, "https://github.com/commercetools/commercetools-jvm-sdk/blob/master");
            return "<div id=\"" + tagId + "%s\" class=code-example>"
                    + (fullFileRequested ?
                    "<button type='button' style='display: none;' class='reveal-imports'>show/hide imports</button>"
                            + "<pre class='hide code-example-imports'><code class='java'>" + htmlEscapedImports + "</code></pre>"
                    : "")
                    + "<pre><code class='java'>" + htmlEscapedBody + "</code><p>See the <a href=\"" + pathToGitHubTestFile + "\" target=\"_blank\">test code</a>.</pre>"
                    + "</div>";
        } catch (final Exception e) {
            System.err.println(e);
            System.err.println("in");
            System.err.println(tag);
            System.err.println(tag.position());
            throw e;
        }

    }

    private String htmlEscape(final String res) {
        return res.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private File findFile(String fullyQualifiedClassName, String partialFilePath, final Tag tag) throws IOException {
        final File cwd = allProjectsBase();
        final File[] directories = cwd.listFiles(file -> file.isDirectory() && !file.getName().startsWith("."));
        boolean found = false;
        File result = null;
        for (final File directory : directories) {
            final List<String> possibleSubfolders = Arrays.asList("/src/test/java", "/src/it/java", "/test/java", "/it/java");
            for (int subIndex = 0; subIndex < possibleSubfolders.size(); subIndex++) {
                final String pathToTest = "/" + directory.getName() + possibleSubfolders.get(subIndex) + "/" + partialFilePath;
                final File attempt = new File(cwd, pathToTest).getCanonicalFile();
                if (attempt.exists()) {
                    if (found) {
                        throw new RuntimeException(format("the class %s exists multiple times.", fullyQualifiedClassName));
                    } else {
                        result = attempt;
                        found = true;
                    }
                }
            }
        }
        if (!found) {
            throw new RuntimeException("cannot find file for " + fullyQualifiedClassName + " for " + tag.position() + " in " + cwd);
        }
        return result;
    }

    private File allProjectsBase() {
        return InternalTagletUtils.allProjectsBaseFile();
    }

    private List<String> fileToArray(File testFile) throws FileNotFoundException {
        final Scanner scanner = new Scanner(testFile);
        List<String> lines = new ArrayList<>();
        while(scanner.hasNext()) {
            lines.add(scanner.nextLine());
        }
        return lines;
    }

    public String getName() {
        return "include.example";
    }

    public boolean inField() {
        return true;
    }

    public boolean inConstructor() {
        return true;
    }

    public boolean inMethod() {
        return true;
    }

    public boolean inOverview() {
        return true;
    }

    public boolean inPackage() {
        return true;
    }

    public boolean inType() {
        return true;
    }

    public boolean isInlineTag() {
        return true;
    }

    @SuppressWarnings("unused")//used by the Javadoc tool
    public static void register(Map<String, Taglet> tagletMap) {
        final CodeTaglet createdTaglet = new CodeTaglet();
        final Taglet t = tagletMap.get(createdTaglet.getName());
        if (t != null) {
            tagletMap.remove(createdTaglet.getName());
        }
        tagletMap.put(createdTaglet.getName(), createdTaglet);
    }

    //only needed for block taglets
    public String toString(Tag[] tags) {
        return null;
    }
}

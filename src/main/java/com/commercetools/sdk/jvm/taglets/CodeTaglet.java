package com.commercetools.sdk.jvm.taglets;

import com.commercetools.build.taglets.InternalTagletUtils;
import com.sun.source.doctree.DocTree;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Taglet;

import javax.lang.model.element.Element;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static com.commercetools.build.taglets.InternalTagletUtils.usableException;
import static java.lang.String.format;

public final class CodeTaglet implements Taglet {

    private DocletEnvironment env;

    @Override
    public void init(final DocletEnvironment env, final Doclet doclet) {
        this.env = env;
    }

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

    @Override
    public String toString(List<? extends DocTree> tags, Element element) {
        final DocTree docTree = tags.get(0);
        final int beginning = 2 + getName().length(); // { + @ + length of the tag name. Used to extract tag text (the part after @)
        final String text = docTree.toString().substring(beginning, docTree.toString().length() - 1).trim();

        try {
            return getString(text, element);
        } catch (Exception e) {
            throw usableException(this, text, element, e);
        }
    }

    private enum Position {
        START, IMPORTS, CODE
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
            if (fullFileRequested) {
                //partially from http://stackoverflow.com/a/326448
                final int fileLength = (int) testFile.length();
                final StringBuilder fileContents = new StringBuilder(fileLength);
                final StringBuilder importStatements = new StringBuilder(fileLength);
                final String lineSeparator = System.getProperty("line.separator");//do not confuse with File.separator
                try (Scanner scanner = new Scanner(testFile)) {
                    Position position = Position.START;
                    while (scanner.hasNextLine()) {
                        final String line = scanner.nextLine();
                        final String trimmedLine = line.trim();
                        if (position != Position.CODE && "".equals(trimmedLine)) {
                            //ignore
                        } else if (position == Position.START && trimmedLine.startsWith("package")) {
                            position = Position.IMPORTS;
                        } else if (position == Position.IMPORTS && trimmedLine.startsWith("import")) {
                            importStatements.append(line).append(lineSeparator);
                        } else if (position == Position.IMPORTS || position == Position.CODE) {
                            position = Position.CODE;
                            fileContents.append(line).append(lineSeparator);
                        } else {
                            throw new IllegalStateException("can't parse Java file");
                        }
                    }
                    res = fileContents.toString();
                    imports = importStatements.toString();
                }
            } else {
                final String testName = tagText.substring(pos + 1).trim();
                final Scanner scanner = new Scanner(testFile);
                List<String> lines = new ArrayList<>();
                boolean endFound = false;
                while(scanner.hasNext() && !endFound) {
                    String current = scanner.findInLine("(public|private|protected) .* " + testName + "\\(.*");
                    final boolean methodStartFound = current != null;
                    if (methodStartFound) {
                        scanner.nextLine();
                        do {
                            current = scanner.nextLine();
                            endFound = current.equals("    }") || current.contains("//end example parsing here");
                            if (!endFound) {
                                final String currentWithoutLeadingWhitespace = current.replaceFirst("        ", "");
                                lines.add(currentWithoutLeadingWhitespace);
                            }
                        } while (!endFound);
                    } else {
                        scanner.nextLine();
                    }
                }

                for (String s : lines) {
                    res += s + "\n";
                }
            }
            final String htmlEscapedBody = htmlEscape(res);
            if ("".equals(htmlEscapedBody)) {
                throw new RuntimeException("Empty example for " + tagText + " in " + testFile.getAbsolutePath());
            }
            final String htmlEscapedImports = htmlEscape(imports);
            final String tagId = tagText.replaceAll("[^a-zA-Z0-9]","-");
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
            System.err.println(element);
            System.err.println(element.getSimpleName());
            throw e;
        }
    }

    private String htmlEscape(final String res) {
        return res.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private File findFile(String fullyQualifiedClassName, String partialFilePath, final Element element) throws IOException {
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
            //TODO check this
            //throw new RuntimeException("cannot find file for " + fullyQualifiedClassName + " for " + tag.position() + " in " + cwd);
            throw new RuntimeException("cannot find file for " + fullyQualifiedClassName + " for " + element.getSimpleName() + " in " + cwd);
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

    public boolean isInlineTag() {
        return true;
    }
}
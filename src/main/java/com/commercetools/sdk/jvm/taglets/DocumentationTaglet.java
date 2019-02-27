package com.commercetools.sdk.jvm.taglets;

import com.sun.source.doctree.DocTree;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Taglet;

import javax.lang.model.element.Element;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.commercetools.build.taglets.InternalTagletUtils.usableException;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

//see http://docs.oracle.com/javase/8/docs/jdk/api/javadoc/doclet/com/sun/javadoc/Tag.html
public final class DocumentationTaglet implements Taglet {

    public static final String FILE_SEPERATOR = System.getProperty("file.separator");
    public static final String UPDATEACTIONS_PACKAGE = "updateactions";
    public static final Predicate<File> FILE_CONTAINS_PUBLIC_UPDATEACTION_PREDICATE =
            file -> readAllLines(file)
                    .stream()
                    .anyMatch(line -> line.contains("public class " + file.getName().replace(".java", "")) || line.contains("public final class " + file.getName().replace(".java", "")));

    private DocletEnvironment env;

    private static List<String> readAllLines(final File file) {
        try {
            return Files.readAllLines(Paths.get(file.toURI()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init(final DocletEnvironment env, final Doclet doclet) {
        this.env = env;
    }

    @Override
    public Set<Location> getAllowedLocations() {
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

    @Override
    public boolean isInlineTag() {
        return true;
    }

    @Override
    public String getName() {
        return "doc.gen";
    }

    private String getString(final String tagText, final Element element) throws IOException {
        String result = null;
        if (isPackage(element)) {
            if (isSummary(tagText)) {
                if (isCommandPackage(element)) {
                    result = format("Provides types to change the state of %s.", furtherArgs(tagText));
                } else if (isUpdateactionsPackage(element)) {
                    result = format("Provides the possible operations which can be performed on update commands for %s.", furtherArgs(tagText));
                } else if (isQueriesPackage(element)) {
                    result = format("Provides types to retrieve the state of %s.", furtherArgs(tagText));
                } else if (isExpansionPackage(element)) {
                    result = format("Provides reference expansion models for %s.", furtherArgs(tagText));
                } else {//model package
                    result = format("Provides model classes and builders for %s.", furtherArgs(tagText));
                }
            }
        } else if (isEntityQueryClass(element)) {
            result = format("Provides a QueryDsl for %s to formulate predicates, search expressions and reference expansion path expressions. " +
                    "<p>For further information how to use the query API to consult the <a href='" + relativeUrlTo(element, "io.sphere.sdk.meta.QueryDocumentation") +
                    "'>Query API documentation</a>.</p>", furtherArgs(tagText));
        } else if (isEntityQueryClassBuilder(element)) {
            final String queryClassSimpleName = getClassName(element).replace("Builder", "");
            result = format("A Builder for <a href='%s.html'>%s</a>.", queryClassSimpleName, queryClassSimpleName);
        } else if (isQueryModelClass(element)) {
            result = format("Provides a domain specific language to formulate predicates and search expressions for querying %s.", furtherArgs(tagText));
        } else if (isUpdateCommandClass(element) && tagText.contains("list actions")) {
            final File currentFile = Objects.requireNonNull(getFile(element), "command dir not found");

            final String folderForUpdateActions = currentFile.getParentFile().getAbsolutePath().replace("/target/generated-sources/annotations/", "/src/main/java/");
            final File updateactionsDirectory = new File(new File(folderForUpdateActions), "updateactions");
            final List<String> updateActionNames =
                    asList(updateactionsDirectory.listFiles((file, name) -> name.endsWith(".java") && !name.contains("-")))
                            .stream()
                            .filter(FILE_CONTAINS_PUBLIC_UPDATEACTION_PREDICATE)
                            .map(file -> file.getName().replace(".java", ""))
                            .sorted()
                            .collect(toList());
            final StringBuilder builder = new StringBuilder("<p id=update-actions>Known UpdateActions</p><ul>");
            updateActionNames.forEach(name -> builder.append(format("<li><a href=\"%s/%s.html\">%s</a></li>", UPDATEACTIONS_PACKAGE, name, name)));
            builder.append("</ul>");
            result = builder.toString();
        } else if (isClientRequestList(tagText)) {
            final Path currentRelativePath = getProjectRoot();
            final ClientRequestListFileVisitor visitor = new ClientRequestListFileVisitor();
            Files.walkFileTree(currentRelativePath, visitor);
            final StringBuilder builder = new StringBuilder("<table border=1><tr><th>resource</th><th>accesors</th><th>mutators</th></tr>");
            final Comparator<? super Map.Entry<String, ResourcesRequests>> comparator = Comparator.comparing(entry -> entry.getKey());
            visitor.getResources().entrySet().stream().sorted(comparator).forEach(entry -> {

                final Function<String, String> mapper = m -> {
                    final String fullClassName = m.substring(m.indexOf("/io/sphere/sdk")).replace(".java", "").replace("/", ".");
                    return "<a href='" + relativeUrlTo(element, fullClassName).replace("//", "/") + "'>" + fullClassNameToSimple(fullClassName) + "</a>";
                };
                final List<String> accessors = entry.getValue().getAccessors().stream().map(mapper).collect(toList());
                final List<String> mutators = entry.getValue().getMutators().stream().map(mapper).collect(toList());
                final int neededLines = Math.max(accessors.size(), mutators.size());
                builder.append("<tr><td rowspan=\"").append(neededLines).append("\">").append(entry.getKey()).append("</td><td>").append(accessors.isEmpty() ? "" : accessors.get(0)).append("</td><td>").append(mutators.isEmpty() ? "" : mutators.get(0)).append("</td></tr>").append("\n");
                for (int i = 1; i < neededLines; i++) {
                    builder.append("<tr><td>").append(accessors.size() > i ? accessors.get(i) : "").append("</td><td>").append(mutators.size() > i ? mutators.get(i) : "").append("</td></tr>").append("\n");
                }
            });
            builder.append("</table>");
            result = builder.toString();
        } else if (isFileInclude(tagText)) {
            throw new RuntimeException("file include is not supported anymore in " + getClass());
        } else if (isIntro(tagText)) {
            result = renderIntro(element);
        }

        //final String s = String.format("firstSentenceTags() %s\n<br>holder() %s\n<br>inlineTags() %s\n<br>kind() %s\n<br>position() %s\n<br>text()\n<br> %s\n<br>toS %s", Arrays.toString(tag.firstSentenceTags()), tag.holder(), Arrays.toString(tag.inlineTags()), tag.kind(), tag.position(), tag.text(), tag.toString());
        if (result == null) {
            throw new RuntimeException(tagText + " is not prepared to be used here: " + element.getSimpleName());
        }
        return result;
    }

    private Path getProjectRoot() {
        return findRoot(new File(".").getAbsoluteFile(), 5).toPath();
    }

    private File findRoot(final File currentDir, final int ttl) {
        if (ttl <= 0) {
            throw new RuntimeException("cannot find root project folder (ttl)");
        } else if (currentDir == null) {
            throw new RuntimeException("cannot find root project folder (dir)");
        }
        final File licenseFile = new File(currentDir, "LICENSE.md");
        if (licenseFile.exists()) {
            return currentDir;
        } else {
            return findRoot(currentDir.getParentFile(), ttl - 1);
        }
    }

    private boolean isEntityQueryClassBuilder(final Element element) {
        final String className = getClassName(element);
        return className.endsWith("QueryBuilder");
    }

    private String renderIntro(final Element element) {
        final File updateActionFile = getFile(element);
        if (isUpdateActionIntro(element)) {
            final File parentFile = updateActionFile.getParentFile().getParentFile();
            final String parentPathInGenerated = parentFile.getAbsolutePath()
                    .replace("/src/main/java/", "/target/generated-sources/annotations/");
            final File updateCommand = Stream.of(parentFile, new File(parentPathInGenerated))
                    .flatMap(file -> Arrays.stream(file.listFiles((dir, fileName) -> fileName.endsWith("UpdateCommand.java"))))
                    .findFirst().orElseThrow(() -> new RuntimeException("command not found for " + updateActionFile));
            final String updateCommandClassName = updateCommand.getName().replace(".java", "");
            final String entityName = updateCommandClassName.replace("UpdateCommand", "");
            return format("<p>See also <a href=\"../%s.html\">%s</a>.<p>", updateCommandClassName, updateCommandClassName);
        } else {
            return null;
        }
    }

    private boolean isUpdateActionIntro(final Element element) {
        return "updateactions".equals(getLastPackageName(element));
    }

    private boolean isIntro(final String tagText) {
        return tagText.startsWith("intro");
    }

    private boolean isFileInclude(final String tagText) {
        return tagText.startsWith("include file ");
    }

    private String fullClassNameToSimple(final String fullClassName) {
        final String[] elements = fullClassName.split("\\.");
        return elements[elements.length - 1];
    }

    private static class ResourcesRequests {
        private final List<String> accessors = new LinkedList<>();
        private final List<String> mutators = new LinkedList<>();

        public void addAccessor(final String element) {
            accessors.add(element);
        }

        public void addMutator(final String element) {
            mutators.add(element);
        }

        public List<String> getAccessors() {
            return accessors;
        }

        public List<String> getMutators() {
            return mutators;
        }

        @Override
        public String toString() {
            return "ResourcesRequests{" +
                    "accessors=" + accessors +
                    ", mutators=" + mutators +
                    '}';
        }
    }

    private static class ClientRequestListFileVisitor implements FileVisitor<Path> {

        private final Map<String, ResourcesRequests> resources = new HashMap<>();

        public Map<String, ResourcesRequests> getResources() {
            if (resources.containsKey("sdk")) {
                resources.remove("sdk");
            }
            return resources;
        }

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            FileVisitResult result = FileVisitResult.CONTINUE;
            final String name = dir.getFileName().toFile().getName();
            if (name.equals("test") || name.equals("it") || name.startsWith(".")) {
                result = FileVisitResult.SKIP_SUBTREE;
            }
            return result;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {

            final File asFile = file.toFile();
            final String name = asFile.getName();
            if(name.endsWith("Command.java")) {
                final String resourceName = asFile.getParentFile().getParentFile().getName();
                get(resourceName).addMutator(asFile.getCanonicalPath());
            } else if(name.endsWith("Query.java") || name.endsWith("Search.java") || name.endsWith("Get.java")) {
                final String resourceName = asFile.getParentFile().getParentFile().getName();
                get(resourceName).addAccessor(asFile.getCanonicalPath());
            }
            return FileVisitResult.CONTINUE;
        }

        private DocumentationTaglet.ResourcesRequests get(final String resourceName) {
            final ResourcesRequests value = resources.getOrDefault(resourceName, new ResourcesRequests());
            resources.put(resourceName, value);
            return value;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }

    private boolean isUpdateCommandClass(final Element element) {
        return getClassName(element).endsWith("UpdateCommand");
    }

    private boolean isQueryModelClass(final Element element) {
        return getClassName(element).endsWith("QueryModel");
    }

    private boolean isClientRequestList(final String tagText) {
        return tagText.equals("list clientrequests");
    }

    private boolean isEntityQueryClass(final Element element) {
        final String className = getClassName(element);
        return className.endsWith("Query");
    }

    private String furtherArgs(final String tagText) {
        final String allArgs = tagText.trim();
        final int startSecondArg = allArgs.indexOf(" ");
        return allArgs.substring(startSecondArg);
    }

    private boolean isSummary(final String tagText) {
        return tagText.startsWith("summary");
    }

    private List<String> fileNamePathSegments(final File file) {
        try {
            return asList(file.getCanonicalPath().split(FILE_SEPERATOR));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isUpdateactionsPackage(final Element element) {
        return getLastPackageName(element).equals(UPDATEACTIONS_PACKAGE);
    }

    private boolean isCommandPackage(final Element element) {
        return getLastPackageName(element).equals("commands");
    }

    private boolean isExpansionPackage(final Element element) {
        return getLastPackageName(element).equals("expansion");
    }

    private boolean isQueriesPackage(final Element element) {
        return getLastPackageName(element).equals("queries");
    }

    private String getLastPackageName(final Element element) {
        final List<String> strings = fileNamePathSegments(new File(this.env.getDocTrees().getPath(element).getCompilationUnit().getSourceFile().toUri()));
        return strings.get(strings.size() - 2);
    }

    private String getClassName(final Element element) {
        return getFile(element).getName().replace(".java", "");
    }

    private String getFullPackage(final Element element) {
        final String absolutePath = getFile(element).getAbsolutePath();
        final String dir = "src/main/java";
        final int codeRootOfThisModule = absolutePath.indexOf(dir) + dir.length() + 1;
        final String substring = absolutePath.substring(codeRootOfThisModule);
        return substring.replace(getFile(element).getName(), "").replace('/', '.');
    }

    private boolean isPackage(Element element) {
        return  getFileName(element).equals("package-info.java");
    }

    private String relativeUrlTo(final Element element, final String fullClassName) {
        final String[] split = getFullPackage(element).split("\\.");
        final int countBack = split.length;
        final StringBuilder builder = new StringBuilder();
        for (final String aSplit : split) {
            builder.append("../");
        }
        return builder.toString() + fullClassName.replace('.', '/') + ".html";
    }

    private String getAbsolutePath(final Element element) {
        return this.env.getDocTrees().getPath(element).getCompilationUnit().getSourceFile().toUri().getPath();
    }

    //TODO print this - might be different
    private String getFileName(final Element element){
        return this.env.getDocTrees().getPath(element).getCompilationUnit().getSourceFile().getName();
    }

    private File getFile(final Element element){
        return new File(this.env.getDocTrees().getPath(element).getCompilationUnit().getSourceFile().toUri());
    }
}
package com.commercetools.taglets;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.sun.source.doctree.DocTree;
import jdk.javadoc.doclet.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.lang.model.element.Element;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.commercetools.taglets.InternalTagletUtils.usableException;

public class ToCTaglet extends BaseTaglet {

    private DocletEnvironment env;
    @Override
    public void init(DocletEnvironment env, Doclet doclet) {
        this.env = env;
    }

    @Override
    public Set<Location> getAllowedLocations() {
        final Set<Location> allowedLocations = new HashSet<>();
        allowedLocations.add(Location.TYPE);
        allowedLocations.add(Location.OVERVIEW);
        return allowedLocations;
    }



    public String getName() {
        return "include.toc";
    }

    public boolean isInlineTag() {
        return true;
    }

    @Override
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

    public String getString(String text, Element element)  throws IOException {

        if (!text.isEmpty()) {
            return buildToCForClass(text, element);
        }

        return buildToC(env.getElementUtils().getDocComment(element));
    }

    public String buildToCForClass(final String className, Element element) throws IOException {

        final File testFile;
        if (!classFiles.containsKey(className)) {
            final String partialFilePath = className.replace('.', '/').concat(".java");

            testFile = findFile(className, partialFilePath, element);
            classFiles.put(className, testFile);
        } else {
            testFile = classFiles.get(className);
        }
        if (!parsedFiles.containsKey(testFile)) {
            parsedFiles.put(testFile, StaticJavaParser.parse(testFile));
        }
        final CompilationUnit parse = parsedFiles.get(testFile);
        final ClassOrInterfaceDeclaration declaration = parse
                .getTypes()
                .stream().filter(typeDeclaration -> typeDeclaration instanceof ClassOrInterfaceDeclaration)
                .map(typeDeclaration -> (ClassOrInterfaceDeclaration)typeDeclaration)
                .filter(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getFullyQualifiedName().orElse("").endsWith(className))
                .collect(Collectors.toList())
                .get(0);
        final String comment = declaration.getJavadocComment().map(javadocComment -> javadocComment.parse().toText()).orElse("");
        return buildToC(comment, declaration.getFullyQualifiedName().orElse(null));
    }

    public String buildToC(final String docComment) {
        return buildToC(Jsoup.parse(docComment), null);
    }

    public String buildToC(final String docComment, final String className) {
        return buildToC(Jsoup.parse(docComment), className);
    }

    private String buildToC(final Document document, final String className) {
        final Optional<String> qualifiedClassName = Optional.ofNullable(className);
        Elements headings;
        if (!qualifiedClassName.isPresent()) {
            headings = document.body().select("h2, h3, h4");
        } else {
            headings = document.body().select("h3, h4");
        }

        org.jsoup.nodes.Element e = headings.first();

        StringBuilder toc = new StringBuilder();
        if (!qualifiedClassName.isPresent()) {
            toc.append("<b>Table of content</b>");
        }
        toc.append("<ul>");
        int opened = 1;
        for (org.jsoup.nodes.Element heading: headings) {
            if (heading.tagName().compareTo(e.tagName()) > 0) {
                toc.append("<ul>");
                opened += 1;
            }
            if (heading.tagName().compareTo(e.tagName()) < 0) {
                toc.append("</ul>");
                opened -= 1;
            }
            toc.append("<li>");
            if (!heading.id().isEmpty() && !qualifiedClassName.isPresent()) {
                    toc.append("<a href=\"#")
                            .append(heading.id())
                            .append("\">");
            }
            if (qualifiedClassName.isPresent()) {
                toc.append("<code>");
            }
            toc.append(heading.html());
            if (qualifiedClassName.isPresent()) {
                toc.append("</code>");
            }
            if (!heading.id().isEmpty() && !qualifiedClassName.isPresent()) {
                toc.append("</a>");
            }
            toc.append("</li>");
            e = heading;
        }
        toc.append(String.join("", Collections.nCopies(opened, "</ul>")));

        return toc.toString();
    }
}

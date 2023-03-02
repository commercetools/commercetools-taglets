package com.commercetools.taglets;

import com.sun.source.doctree.DocTree;
import jdk.javadoc.doclet.Taglet;

import javax.lang.model.element.Element;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public final class FileIncludeTaglet implements Taglet {

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
    public boolean isInlineTag() {
        return true;
    }

    public String toString(List<? extends DocTree> tags, Element element) {
        final DocTree docTree = tags.get(0);
        final int beginning = 2 + getName().length(); // { + @ + length of the tag name. Used to extract tag text (the part after @)
        final String text = docTree.toString().substring(beginning, docTree.toString().length() - 1).trim();
        try {
            return getString(text, element);
        }
        catch (Exception e) {
            throw new RuntimeException(String.format("Failed to include with {%s} at %s with base %s", text, beginning, InternalTagletUtils.allProjectsBaseFile()), e);
        }
    }

    private String getString(final String tagText, final Element element) throws IOException {
        final File file = new File(InternalTagletUtils.allProjectsBaseFile(), tagText);
        final String fileContents = new String(Files.readAllBytes(file.toPath()));
        final String htmlEscapedBody = htmlEscape(fileContents);
        final String tagId = tagText.replaceAll("[^a-zA-Z0-9]","-");
        final String codeCssClass = tagText.endsWith(".java") ? "java" : "";
        final String absolutePath = file.getAbsolutePath();
        final String canonicalPath = new File(".").getAbsoluteFile().getCanonicalPath().replace("/build/docs", "");
        final String pathToGitHubTestFile = absolutePath.replace(canonicalPath,
                "https://github.com/commercetools/commercetools-sdk-java-v2/blob/master");
        return "<div id='" + tagId + "' style='background: #f0f0f0;'>"
                + "<pre><a href=\"" + pathToGitHubTestFile + "\" target=\"_blank\">" + file.getName() + "</a>:<br/>"
                + "<code class='" + codeCssClass + "'>" + htmlEscapedBody + "</code></pre>"
                + "</div>";
    }

    private String htmlEscape(final String res) {
        return res.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public String getName() {
        return "include.file";
    }
}

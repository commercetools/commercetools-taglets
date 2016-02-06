package com.commercetools.build.maven.taglets;

import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public final class FileIncludeTaglet implements Taglet {

    /**
     * Generates the String output for a tag
     * @param tag
     * @return
     */
    public String toString(Tag tag) {
        try {
            return getString(tag);
        } catch (final Exception e) {
            System.err.println(e);
            System.err.println("in");
            System.err.println(tag);
            System.err.println(tag.position());
            throw new RuntimeException(e);
        }
    }

    private String getString(final Tag tag) throws IOException {
        final String relativeFilePath = tag.text();
        final File file = new File(new File(".").getAbsolutePath().replace("/target/site/apidocs", ""), relativeFilePath);
        final String fileContents = new String(Files.readAllBytes(file.toPath()));
        final String htmlEscapedBody = htmlEscape(fileContents);
        final String tagId = relativeFilePath.replaceAll("[^a-zA-Z0-9]","-");
        final String codeCssClass = relativeFilePath.endsWith(".java") ? "java" : "";

        return "<div id='" + tagId + "' style='background: #f0f0f0;'>"
                + "<pre><code class='" + codeCssClass + "'>" + htmlEscapedBody + "</code></pre>"
                + "</div>";
    }

    private String htmlEscape(final String res) {
        return res.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public String getName() {
        return "include.file";
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
        final FileIncludeTaglet createdTaglet = new FileIncludeTaglet();
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
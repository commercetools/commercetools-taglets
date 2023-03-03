package com.commercetools.taglets;

import com.sun.source.doctree.DocTree;
import jdk.javadoc.doclet.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.lang.model.element.Element;
import javax.tools.JavaFileManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ToCTaglet implements Taglet {

    private DocletEnvironment env;
    @Override
    public void init(DocletEnvironment env, Doclet doclet) {
        this.env = env;
    }

    @Override
    public Set<Location> getAllowedLocations() {
        final Set<Location> allowedLocations = new HashSet<>();
        allowedLocations.add(Location.TYPE);
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
        String t = env.getElementUtils().getDocComment(element);
        final Document parse = Jsoup.parse(t);

        Elements headings = parse.body().select("h2, h3, h4");

        org.jsoup.nodes.Element e = headings.first();

        StringBuilder toc = new StringBuilder("");
        toc.append("<b>Table of content</b>")
           .append("<ul>");
        boolean opened = false;
        for (org.jsoup.nodes.Element heading: headings) {
            if (heading.tagName().compareTo(e.tagName()) > 0) {
                toc.append("<ul>");
                opened = true;
            }

            toc.append("<li>");
            if (!heading.id().equals("")) {
                toc.append("<a href=\"#")
                   .append(heading.id())
                   .append("\">");
            }
            toc.append(heading.html());
            if (!heading.id().equals("")) {
                toc.append("</a>");
            }
            toc.append("</li>");
            if (heading.tagName().compareTo(e.tagName()) < 0) {
                toc.append("</ul>");
            }
            e = heading;
        }
        if (opened)
            toc.append("</ul>");
        toc.append("</ul>");
        return toc.toString();
    }
}

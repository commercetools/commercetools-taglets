package com.commercetools.taglets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ToCTagletTest {
    private final static String comment = "{@include.toc}\n" +
            "<h2 id=\"heading\">Heading</h2>\n" +
            "<h3 id=\"heading-1\">Heading 1</h3>\n" +
            "<h3 id=\"heading-2\">Heading 2</h3>\n" +
            "<h4 id=\"heading-2-1\">Heading 2-1</h4>\n" +
            "<h4 id=\"heading-2-2\">Heading 2-2</h4>\n" +
            "<h3 id=\"heading-3\">Heading 3</h3>\n" +
            "<h3 id=\"heading-4\">Heading 4</h3>\n";

    private final static String comment2 = "{@include.toc}\n" +
            "<h2 id=\"heading\">Heading</h2>\n" +
            "<h3 id=\"heading-1\">Heading 1</h3>\n" +
            "<h3 id=\"heading-2\">Heading 2</h3>\n" +
            "<h4 id=\"heading-2-1\">Heading 2-1</h4>\n" +
            "<h4 id=\"heading-2-2\">Heading 2-2</h4>\n";

    @Test
    public void testToC()
    {
        ToCTaglet t = new ToCTaglet();

        String toc = t.buildToC(comment);

        String expectedToc = "<b>Table of content</b>" +
                "<ul>" +
                    "<li><a href=\"#heading\">Heading</a></li>" +
                    "<ul>" +
                        "<li><a href=\"#heading-1\">Heading 1</a></li>" +
                        "<li><a href=\"#heading-2\">Heading 2</a></li>" +
                        "<ul>" +
                            "<li><a href=\"#heading-2-1\">Heading 2-1</a></li>" +
                            "<li><a href=\"#heading-2-2\">Heading 2-2</a></li>" +
                        "</ul>" +
                        "<li><a href=\"#heading-3\">Heading 3</a></li>" +
                        "<li><a href=\"#heading-4\">Heading 4</a></li>" +
                    "</ul>" +
                "</ul>";
        Assertions.assertEquals(expectedToc, toc);
    }

    @Test
    public void testToC2()
    {
        ToCTaglet t = new ToCTaglet();

        String toc = t.buildToC(comment2);

        String expectedToc = "<b>Table of content</b>" +
                "<ul>" +
                    "<li><a href=\"#heading\">Heading</a></li>" +
                    "<ul>" +
                        "<li><a href=\"#heading-1\">Heading 1</a></li>" +
                        "<li><a href=\"#heading-2\">Heading 2</a></li>" +
                        "<ul>" +
                            "<li><a href=\"#heading-2-1\">Heading 2-1</a></li>" +
                            "<li><a href=\"#heading-2-2\">Heading 2-2</a></li>" +
                        "</ul>" +
                    "</ul>" +
                "</ul>";
        Assertions.assertEquals(expectedToc, toc);
    }
}

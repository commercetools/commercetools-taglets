package com.commercetools.taglets;

import example.TocTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

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

    @Test
    public void testToCForClass() throws IOException {
        ToCTaglet t = new ToCTaglet();

        String toc = t.buildToCForClass(TocTest.class.getCanonicalName(), new DocTest());
        String expectedToc =
                "<ul>" +
                    "<li><code>Heading 1</code></li>" +
                    "<li><code>Heading 2</code></li>" +
                    "<ul>" +
                        "<li><code>Heading 2-1</code></li>" +
                        "<li><code>Heading 2-2</code></li>" +
                    "</ul>" +
                    "<li><code>Heading 3</code></li>" +
                    "<li><code>Heading 4</code></li>" +
                "</ul>";
        Assertions.assertEquals(expectedToc, toc);

    }

    static class DocTest implements Element {
        @Override
        public TypeMirror asType() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return null;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return null;
        }

        @Override
        public Name getSimpleName() {
            return null;
        }

        @Override
        public Element getEnclosingElement() {
            return null;
        }

        @Override
        public List<? extends Element> getEnclosedElements() {
            return null;
        }

        @Override
        public List<? extends AnnotationMirror> getAnnotationMirrors() {
            return null;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            return null;
        }

        @Override
        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            return null;
        }

        @Override
        public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
            return null;
        }
    }
}

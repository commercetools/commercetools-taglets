package com.commercetools.build.taglets;

import com.sun.source.doctree.DocTreeVisitor;
import com.sun.source.doctree.TextTree;
import org.junit.Assert;
import org.junit.Test;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

public class CodeTagletTest {
    static class IncludeExampleTag implements TextTree {
        private String include;

        public IncludeExampleTag(String include) {
            this.include = include;
        }

        @Override
        public String getBody() {
            return include;
        }

        @Override
        public Kind getKind() {
            return null;
        }

        @Override
        public <R, D> R accept(DocTreeVisitor<R, D> visitor, D data) {
            return null;
        }

        @Override
        public String toString() {
            return "{@include.example " + include + "}";
        }
    }

    static class IncludeFileTag implements TextTree {
        private String include;

        public IncludeFileTag(String include) {
            this.include = include;
        }

        @Override
        public String getBody() {
            return include;
        }

        @Override
        public Kind getKind() {
            return null;
        }

        @Override
        public <R, D> R accept(DocTreeVisitor<R, D> visitor, D data) {
            return null;
        }

        @Override
        public String toString() {
            return "{@include.file " + include + "}";
        }
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

    @Test
    public void testExample()
    {
        TextTree tag = new IncludeExampleTag("example.AddressExample#withMethods()");

        CodeTaglet t = new CodeTaglet();
        String example = t.toString(List.of(tag), new DocTest());

        String withMethods = "<div id=\"example-AddressExample-withMethods--%s\" class=code-example><pre><code class='java'>final Address addressWithContactData = address\n" +
                "        .withEmail(\"hello@commercetools.de\")\n" +
                "        .withPhone(\"+49.89.99 82 996-0\");\n" +
                "</code><p>See the <a href=\"https://github.com/commercetools/commercetools-sdk-java-v2/blob/master/src/test/java/example/AddressExample.java\" target=\"_blank\">test code</a>.</pre></div>";
        Assert.assertEquals(withMethods, example);
    }

    @Test
    public void testExampleWithoutBrackets()
    {
        TextTree tag = new IncludeExampleTag("example.AddressExample#withMethods");

        CodeTaglet t = new CodeTaglet();
        String example = t.toString(List.of(tag), new DocTest());

        String withMethods = "<div id=\"example-AddressExample-withMethods%s\" class=code-example><pre><code class='java'>final Address addressWithContactData = address\n" +
                "        .withEmail(\"hello@commercetools.de\")\n" +
                "        .withPhone(\"+49.89.99 82 996-0\");\n" +
                "</code><p>See the <a href=\"https://github.com/commercetools/commercetools-sdk-java-v2/blob/master/src/test/java/example/AddressExample.java\" target=\"_blank\">test code</a>.</pre></div>";
        Assert.assertEquals(withMethods, example);
    }
    @Test
    public void testInterfaceExample()
    {
        TextTree tag = new IncludeExampleTag("example.Address#of()");

        CodeTaglet t = new CodeTaglet();
        String example = t.toString(List.of(tag), new DocTest());

        String withMethods = "<div id=\"example-Address-of--%s\" class=code-example><pre><code class='java'>return address.getElements();\n" +
                "</code><p>See the <a href=\"https://github.com/commercetools/commercetools-sdk-java-v2/blob/master/src/test/java/example/Address.java\" target=\"_blank\">test code</a>.</pre></div>";
        Assert.assertEquals(withMethods, example);
    }

    @Test
    public void testFullfileExample()
    {
        TextTree tag = new IncludeExampleTag("example.Address");

        CodeTaglet t = new CodeTaglet();
        String example = t.toString(List.of(tag), new DocTest());

        String file = "<div id=\"example-Address%s\" class=code-example><button type='button' style='display: none;' class='reveal-imports'>show/hide imports</button><pre class='hide code-example-imports'><code class='java'>import java.util.List;\n" +
                "</code></pre><pre><code class='java'>public interface Address {\n" +
                "    Address withEmail(String value);\n" +
                "    Address withPhone(String value);\n" +
                "    List&lt;String&gt; getElements();\n" +
                "\n" +
                "    public static List&lt;String&gt; of(Address address) {\n" +
                "        return address.getElements();\n" +
                "    }\n" +
                "}\n" +
                "</code><p>See the <a href=\"https://github.com/commercetools/commercetools-sdk-java-v2/blob/master/src/test/java/example/Address.java\" target=\"_blank\">test code</a>.</pre></div>";
        Assert.assertEquals(file, example);
    }

    @Test
    public void testInnerClassTestExample()
    {
        TextTree tag = new IncludeExampleTag("example.InnerClassTest");

        CodeTaglet t = new CodeTaglet();
        String example = t.toString(List.of(tag), new DocTest());

        String file = "<div id=\"example-InnerClassTest%s\" class=code-example><button type='button' style='display: none;' class='reveal-imports'>show/hide imports</button><pre class='hide code-example-imports'><code class='java'></code></pre><pre><code class='java'>public class InnerClassTest {\n" +
                "    public void innerClass() {\n" +
                "        class InnerClass {\n" +
                "        }\n" +
                "\n" +
                "        final int i = 0;\n" +
                "    }\n" +
                "}\n" +
                "</code><p>See the <a href=\"https://github.com/commercetools/commercetools-sdk-java-v2/blob/master/src/test/java/example/InnerClassTest.java\" target=\"_blank\">test code</a>.</pre></div>";
        Assert.assertEquals(file, example);
    }

    @Test
    public void testFileInclude()
    {
        TextTree tag = new IncludeFileTag("src/test/java/example/Address.java");

        FileIncludeTaglet t = new FileIncludeTaglet();
        String example = t.toString(List.of(tag), new DocTest());

        String withMethods = "<div id='src-test-java-example-Address-java' style='background: #f0f0f0;'><pre><a href=\"https://github.com/commercetools/commercetools-sdk-java-v2/blob/master/src/test/java/example/Address.java\" target=\"_blank\">Address.java</a>:<br/><code class='java'>package example;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "public interface Address {\n" +
                "    Address withEmail(String value);\n" +
                "    Address withPhone(String value);\n" +
                "    List&lt;String&gt; getElements();\n" +
                "\n" +
                "    public static List&lt;String&gt; of(Address address) {\n" +
                "        return address.getElements();\n" +
                "    }\n" +
                "}\n" +
                "</code></pre></div>";
        Assert.assertEquals(withMethods, example);
    }
}

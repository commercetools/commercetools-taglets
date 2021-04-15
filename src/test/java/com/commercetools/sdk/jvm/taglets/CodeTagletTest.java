package com.commercetools.sdk.jvm.taglets;

import com.sun.javadoc.Tag;
import com.sun.tools.doclets.internal.toolkit.util.TextTag;
import org.junit.Assert;
import org.junit.Test;

public class CodeTagletTest {
    @Test
    public void testExample()
    {
        Tag tag = new TextTag(null, "example.AddressExample#withMethods()");
        CodeTaglet t = new CodeTaglet();
        String example = t.toString(tag);

        String withMethods = "<div id=\"example-AddressExample-withMethods--%s\" class=code-example><pre><code class='java'>final Address addressWithContactData = address\n" +
                "        .withEmail(\"hello@commercetools.de\")\n" +
                "        .withPhone(\"+49.89.99 82 996-0\");\n" +
                "</code><p>See the <a href=\"https://github.com/commercetools/commercetools-jvm-sdk/blob/master/src/test/java/example/AddressExample.java\" target=\"_blank\">test code</a>.</pre></div>";
        Assert.assertEquals(withMethods, example);
    }

    @Test
    public void testExampleWithoutBrackets()
    {
        Tag tag = new TextTag(null, "example.AddressExample#withMethods");
        CodeTaglet t = new CodeTaglet();
        String example = t.toString(tag);

        String withMethods = "<div id=\"example-AddressExample-withMethods%s\" class=code-example><pre><code class='java'>final Address addressWithContactData = address\n" +
                "        .withEmail(\"hello@commercetools.de\")\n" +
                "        .withPhone(\"+49.89.99 82 996-0\");\n" +
                "</code><p>See the <a href=\"https://github.com/commercetools/commercetools-jvm-sdk/blob/master/src/test/java/example/AddressExample.java\" target=\"_blank\">test code</a>.</pre></div>";
        Assert.assertEquals(withMethods, example);
    }

    @Test
    public void testInterfaceExample()
    {
        Tag tag = new TextTag(null, "example.Address#of()");
        CodeTaglet t = new CodeTaglet();
        String example = t.toString(tag);

        String withMethods = "<div id=\"example-Address-of--%s\" class=code-example><pre><code class='java'>return address.getElements();\n" +
                "</code><p>See the <a href=\"https://github.com/commercetools/commercetools-jvm-sdk/blob/master/src/test/java/example/Address.java\" target=\"_blank\">test code</a>.</pre></div>";
        Assert.assertEquals(withMethods, example);
    }

    @Test
    public void testFullfileExample()
    {
        Tag tag = new TextTag(null, "example.Address");
        CodeTaglet t = new CodeTaglet();
        String example = t.toString(tag);

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
                "</code><p>See the <a href=\"https://github.com/commercetools/commercetools-jvm-sdk/blob/master/src/test/java/example/Address.java\" target=\"_blank\">test code</a>.</pre></div>";
        Assert.assertEquals(file, example);
    }
}

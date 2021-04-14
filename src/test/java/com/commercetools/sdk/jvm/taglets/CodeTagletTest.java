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

        Assert.assertNotNull(example);
    }
}

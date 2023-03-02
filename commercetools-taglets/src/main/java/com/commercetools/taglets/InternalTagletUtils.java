
package com.commercetools.taglets;

import static java.lang.String.format;

import java.io.File;
import java.util.Arrays;

import javax.lang.model.element.Element;

import jdk.javadoc.doclet.Taglet;

public final class InternalTagletUtils {
    private InternalTagletUtils() {
    }

    public static File allProjectsBaseFile() {
        final File parentFile = new File(new File(".").getAbsoluteFile()
                                                      .getAbsolutePath()
                                                      .replace("/target/site/apidocs", "")
                                                      .replace("/target/apidocs", "")).getParentFile();
        final boolean isAlreadyAllRoot = new File(parentFile, ".gitignore").exists();
        return isAlreadyAllRoot ? parentFile : parentFile.getParentFile().getAbsoluteFile();
    }

    public static RuntimeException usableException(final Taglet taglet, final String tagText, final Element element,
                                                   final Exception e) {
        return new RuntimeException(String.format("error in taglet %s with tag %s for source %s, stacktrace: %s", taglet,
                tagText, element.getSimpleName(), Arrays.toString(e.getStackTrace())), e);
    }
}

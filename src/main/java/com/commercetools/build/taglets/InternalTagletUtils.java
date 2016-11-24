package com.commercetools.build.taglets;

import com.commercetools.sdk.jvm.taglets.CodeTaglet;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

import java.io.File;
import java.util.Arrays;

import static java.lang.String.format;

public final class InternalTagletUtils {
    private InternalTagletUtils() {
    }

    public static File allProjectsBaseFile() {
        final File parentFile = new File(new File(".").getAbsoluteFile().getAbsolutePath().replace("/target/site/apidocs", "").replace("/target/apidocs", ""))
                .getParentFile();
        final boolean isAlreadyAllRoot = new File(parentFile, ".gitignore").exists();
        return isAlreadyAllRoot ?  parentFile : parentFile.getParentFile().getAbsoluteFile();
    }

    public static RuntimeException usableException(final Taglet taglet, final Tag tag, final Exception e) {
        return new RuntimeException(format("error in taglet %s with tag %s for source %s, stacktrace: %s", taglet, tag, tag.position(), Arrays.toString(e.getStackTrace())), e);
    }
}
